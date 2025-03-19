package vn.com.iuh.fit.order_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.event.ValidateInventoryEvent;
import vn.com.iuh.fit.order_service.service.OrderService;

@Service
public class OrderConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);
    private final OrderService orderService;
    private final KafkaTemplate<String, ValidateInventoryEvent> kafkaTemplate;

    public OrderConsumer(OrderService orderService, KafkaTemplate<String, ValidateInventoryEvent> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "${cart.checkout.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "checkoutEventListenerFactory"
    )
    public void processCheckoutEvent(CheckoutEventDTO checkoutEvent, Acknowledgment acknowledgment) {
        try {
            LOGGER.info("📥 Nhận CheckoutEvent từ Kafka: {}", checkoutEvent);
            orderService.createOrderFromCheckout(checkoutEvent);

            // ✅ Đảm bảo commit offset sau khi xử lý xong
            acknowledgment.acknowledge();
        } catch (Exception e) {
            LOGGER.error("❌ Lỗi xử lý Checkout Event: ", e);
        }
    }

}
