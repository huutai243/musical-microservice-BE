package vn.com.iuh.fit.cart_service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.cart_service.event.CheckoutEvent;

@Service
public class CartProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CartProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CartProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCheckoutEvent(CheckoutEvent event) {
        kafkaTemplate.send("checkout-events", event);
        LOGGER.info("Đã gửi CheckoutEvent đến Kafka: [eventId={}, userId={}]", event.getEventId(), event.getUserId());
    }
}

