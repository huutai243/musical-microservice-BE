package vn.com.iuh.fit.order_service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.event.*;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String topic, Object event) {
        kafkaTemplate.send(topic, event);
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        sendEvent("order-events", event);
    }

    public void sendOrderConfirmedEvent(OrderConfirmedEvent event) {
        sendEvent("order-confirmed-events", event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        sendEvent("order-cancelled-events", event);
    }

    public void sendOrderShippedEvent(OrderShippedEvent event) {
        sendEvent("order-shipped-events", event);
    }

    public void sendOrderDeliveredEvent(OrderDeliveredEvent event) {
        sendEvent("order-delivered-events", event);
    }
}
