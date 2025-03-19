package vn.com.iuh.fit.inventory_service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.event.ValidateInventoryEvent;
import vn.com.iuh.fit.inventory_service.service.InventoryService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryConsumer {

    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "inventory-validation-events",
            groupId = "inventory-group",
            containerFactory = "validateInventoryEventListenerFactory"
    )
    public void processInventoryValidation(ValidateInventoryEvent event) {
        try {
            log.info("Nhận yêu cầu kiểm tra tồn kho từ `order-service` cho đơn hàng #{}", event.getOrderId());

            // Chuyển ValidateInventoryEvent.Item → InventoryValidationItem
            List<InventoryValidationItem> items = event.getItems().stream()
                    .map(item -> {
                        try {
                            return new InventoryValidationItem(
                                    Long.parseLong(item.getProductId()),
                                    item.getQuantity()
                            );
                        } catch (NumberFormatException e) {
                            log.error(" Lỗi chuyển productId '{}' - không phải số hợp lệ!", item.getProductId(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!items.isEmpty()) {
                // Gọi Service kiểm tra tồn kho
                inventoryService.validateInventory(event.getOrderId(), items);
            } else {
                log.warn(" Không có sản phẩm hợp lệ để kiểm tra tồn kho cho đơn hàng #{}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error(" Lỗi khi xử lý sự kiện Kafka: ", e);
        }
    }
}
