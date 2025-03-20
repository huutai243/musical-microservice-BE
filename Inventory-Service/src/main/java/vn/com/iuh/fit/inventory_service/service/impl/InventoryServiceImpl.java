package vn.com.iuh.fit.inventory_service.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.entity.Inventory;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.inventory_service.producer.InventoryProducer;
import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;
import vn.com.iuh.fit.inventory_service.service.InventoryService;

import java.util.ArrayList;
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
        List<InventoryValidationResultEvent.Item> validatedItems = new ArrayList<>();

        for (InventoryValidationItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId()).orElse(null);
            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                validatedItems.add(new InventoryValidationResultEvent.Item(
                        item.getProductId().toString(),
                        item.getQuantity(),
                        inventory != null ? inventory.getQuantity() : 0,
                        "OUT_OF_STOCK"
                ));

                if (inventory != null) {
                    inventory.setQuantity(0);
                    inventoryRepository.save(inventory);
                }
            } else {
                // Trừ số lượng sản phẩm khỏi kho
                inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
                inventoryRepository.save(inventory);

                validatedItems.add(new InventoryValidationResultEvent.Item(
                        item.getProductId().toString(),
                        item.getQuantity(),
                        inventory.getQuantity(),
                        "CONFIRMED"
                ));
            }
        }

        // Xác định trạng thái đơn hàng
        String status;
        String message;

        boolean allItemsOutOfStock = validatedItems.stream()
                .allMatch(item -> "OUT_OF_STOCK".equals(item.getStatus()));

        if (validatedItems.stream().noneMatch(item -> "OUT_OF_STOCK".equals(item.getStatus()))) {
            status = "VALIDATED";
            message = "Tồn kho hợp lệ.";
        } else if (allItemsOutOfStock) {
            status = "REJECTED";
            message = "Toàn bộ sản phẩm trong đơn hàng đã hết hàng.";
        } else {
            status = "PARTIALLY_VALIDATED";
            message = "Một số sản phẩm trong đơn hàng không đủ số lượng.";
        }

        // Gửi kết quả kiểm tra tồn kho về `order-service`
        inventoryProducer.sendInventoryValidationResult(
                new InventoryValidationResultEvent(orderId, status, message, validatedItems)
        );
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
