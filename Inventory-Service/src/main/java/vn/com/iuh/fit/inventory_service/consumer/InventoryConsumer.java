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
            log.info(" Nhận yêu cầu kiểm tra tồn kho từ `Order-Service` - Đơn hàng #{}", event.getOrderId());
            List<InventoryValidationItem> items = event.getItems().stream()
                    .map(item -> {
                        try {
                            Long productId = Long.parseLong(item.getProductId());

                            if (item.getQuantity() <= 0) {
                                log.warn(" Số lượng sản phẩm `{}` không hợp lệ: {}", productId, item.getQuantity());
                                return null;
                            }

                            return new InventoryValidationItem(productId, item.getQuantity());

                        } catch (NumberFormatException e) {
                            log.error(" Lỗi chuyển đổi productId `{}`: Không phải số hợp lệ!", item.getProductId(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!items.isEmpty()) {
                inventoryService.validateInventory(event.getOrderId(), items);
            } else {
                log.warn(" Không có sản phẩm hợp lệ để kiểm tra tồn kho cho đơn hàng #{}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error(" Lỗi khi xử lý sự kiện Kafka: ", e);
        }
    }
}

