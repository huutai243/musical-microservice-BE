package vn.com.iuh.fit.order_service.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.event.ValidateInventoryEvent;

@Slf4j
@Service
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryValidationEvent(ValidateInventoryEvent event) {
        try {
            kafkaTemplate.send("inventory-validation-events", event).get();
            log.info("Đã gửi sự kiện kiểm tra tồn kho cho Order ID: {}", event.getOrderId());
        } catch (Exception ex) {
            log.error("Lỗi khi gửi sự kiện Kafka: ", ex);
        }
    }

    public void sendOrderStatusEvent(String topic, Long orderId) {
        try {
            kafkaTemplate.send(topic, orderId).get();
            log.info("Đã gửi sự kiện trạng thái Order: {} -> {}", orderId, topic);
        } catch (Exception ex) {
            log.error("Lỗi khi gửi sự kiện trạng thái: ", ex);
        }
    }
}

