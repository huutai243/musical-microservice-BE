package vn.com.iuh.fit.inventory_service.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.entity.Inventory;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.inventory_service.event.ValidateInventoryEvent;
import vn.com.iuh.fit.inventory_service.producer.InventoryProducer;
import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;
import vn.com.iuh.fit.inventory_service.service.InventoryService;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryProducer inventoryProducer;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryProducer inventoryProducer) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryProducer = inventoryProducer;
    }

    @Override
    public int getStock(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0);
    }

    @Transactional
    @Override
    public void updateStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(null, productId, 0));

        inventory.setQuantity(quantity);
        inventoryRepository.save(inventory);
    }

    /**
     * Kiểm tra tồn kho và gửi phản hồi đến `order-service`
     */
    @Override
    @Transactional
    public void validateInventory(Long orderId, List<InventoryValidationItem> items) {
        boolean allAvailable = true;
        StringBuilder message = new StringBuilder("Sản phẩm thiếu hàng: ");

        for (InventoryValidationItem item : items) {
            int availableStock = getStock(item.getProductId()); // Kiểm tra tồn kho
            if (availableStock < item.getQuantity()) {
                allAvailable = false;
                message.append("ProductID: ").append(item.getProductId()).append(" (Còn: ")
                        .append(availableStock).append(", Cần: ").append(item.getQuantity()).append("); ");
            }
        }

        String status = allAvailable ? "VALIDATED" : "REJECTED";
        String finalMessage = allAvailable ? "Tồn kho hợp lệ." : message.toString();

        // Gửi kết quả kiểm tra tồn kho đến `order-service`
        inventoryProducer.sendInventoryValidationResult(new InventoryValidationResultEvent(orderId, status, finalMessage));
    }



    @Override
    @Transactional
    public void reserveStock(Long orderId, List<InventoryValidationItem> items) {
        for (InventoryValidationItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    @Override
    @Transactional
    public void releaseStock(Long orderId, List<InventoryValidationItem> items) {
        for (InventoryValidationItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElse(new Inventory(null, item.getProductId(), 0));

            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }
}
