package vn.com.iuh.fit.inventory_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.entity.Inventory;
import vn.com.iuh.fit.inventory_service.entity.OutboxEvent;
import vn.com.iuh.fit.inventory_service.event.InventoryDeductionRequestEvent;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.inventory_service.producer.InventoryProducer;
import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;
import vn.com.iuh.fit.inventory_service.repository.OutboxEventRepository;
import vn.com.iuh.fit.inventory_service.service.InventoryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryProducer inventoryProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;


    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryProducer inventoryProducer,ObjectMapper objectMapper,OutboxEventRepository outboxEventRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryProducer = inventoryProducer;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
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
                validatedItems.add(new InventoryValidationResultEvent.Item(
                        item.getProductId().toString(),
                        item.getQuantity(),
                        inventory.getQuantity(),
                        "CONFIRMED"
                ));
            }
        }

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
//        inventoryProducer.sendInventoryValidationResult(
//                new InventoryValidationResultEvent(orderId, status, message, validatedItems)
//        );
        try {
            String payload = objectMapper.writeValueAsString(
                    new InventoryValidationResultEvent(orderId, status, message, validatedItems)
            );

            outboxEventRepository.save(
                    OutboxEvent.builder()
                            .id(UUID.randomUUID())
                            .aggregateType("Inventory")
                            .aggregateId(String.valueOf(orderId))
                            .type("InventoryValidationResultEvent")
                            .payload(payload)
                            .status("PENDING")
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            log.info("Đã ghi InventoryValidationResultEvent vào Outbox thành công cho orderId: {}", orderId);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi serialize InventoryValidationResultEvent", e);
            throw new RuntimeException("Không thể serialize sự kiện kiểm tra tồn kho", e);
        }
    }

    @Override
    @Transactional
    public void deductStock(Long orderId, List<InventoryDeductionRequestEvent.ProductQuantity> products) {
        LOGGER.info(" Trừ tồn kho cho đơn hàng #{} với {} sản phẩm", orderId, products.size());

        for (InventoryDeductionRequestEvent.ProductQuantity item : products) {
            Long productId = Long.parseLong(item.getProductId());
            Integer quantity = item.getQuantity();

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException(" Sản phẩm không tồn tại: " + productId));
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventoryRepository.save(inventory);

            LOGGER.info(" Trừ {} sản phẩm [{}] thành công. Còn lại: {}", quantity, productId, inventory.getQuantity());
        }

        LOGGER.info(" Hoàn tất trừ kho cho đơn hàng #{}", orderId);
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
