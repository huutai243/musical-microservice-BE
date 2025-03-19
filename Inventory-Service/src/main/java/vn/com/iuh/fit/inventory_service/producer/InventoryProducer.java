package vn.com.iuh.fit.inventory_service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;

@Service
public class InventoryProducer {
    private static final Logger log = LoggerFactory.getLogger(InventoryProducer.class);
    private final KafkaTemplate<String, InventoryValidationResultEvent> kafkaTemplate;

    public InventoryProducer(KafkaTemplate<String, InventoryValidationResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryValidationResult(InventoryValidationResultEvent event) {
        log.info("📤 Gửi kết quả kiểm tra tồn kho: Đơn hàng #{} | Trạng thái: {} | Chi tiết: {}",
                event.getOrderId(), event.getStatus(), event.getMessage());
        kafkaTemplate.send("inventory-validation-results", event);
    }
}
