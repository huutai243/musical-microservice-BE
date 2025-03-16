package vn.com.iuh.fit.cart_service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.cart_service.event.CheckoutEvent;

@Service
public class CartProducer {
    private final KafkaTemplate<String, CheckoutEvent> kafkaTemplate;

    public CartProducer(KafkaTemplate<String, CheckoutEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCheckoutEvent(CheckoutEvent event) {
        kafkaTemplate.send("checkout-events", event);
    }
}
