package vn.com.iuh.fit.inventory_service.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;

@Slf4j
@Service
public class InventoryProducer {

    private final KafkaTemplate<String, InventoryValidationResultEvent> kafkaTemplate;

    public InventoryProducer(KafkaTemplate<String, InventoryValidationResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryValidationResult(InventoryValidationResultEvent event) {
        try {
            kafkaTemplate.send("inventory-validation-result", event).get();
            log.info(" Gửi kết quả kiểm tra tồn kho: Đơn hàng #{} | Trạng thái: {} | Chi tiết: {}",
                    event.getOrderId(), event.getStatus(), event.getMessage());
        } catch (Exception e) {
            log.error(" Lỗi gửi Kafka (InventoryValidationResult): ", e);
        }
    }
}
