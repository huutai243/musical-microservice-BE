package vn.com.iuh.fit.order_service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.event.*;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate;
    private final KafkaTemplate<String, OrderConfirmedEvent> orderConfirmedKafkaTemplate;
    private final KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate;
    private final KafkaTemplate<String, OrderShippedEvent> orderShippedKafkaTemplate;
    private final KafkaTemplate<String, OrderDeliveredEvent> orderDeliveredKafkaTemplate;

    public OrderProducer(
            KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate,
            KafkaTemplate<String, OrderConfirmedEvent> orderConfirmedKafkaTemplate,
            KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate,
            KafkaTemplate<String, OrderShippedEvent> orderShippedKafkaTemplate,
            KafkaTemplate<String, OrderDeliveredEvent> orderDeliveredKafkaTemplate
    ) {
        this.orderCreatedKafkaTemplate = orderCreatedKafkaTemplate;
        this.orderConfirmedKafkaTemplate = orderConfirmedKafkaTemplate;
        this.orderCancelledKafkaTemplate = orderCancelledKafkaTemplate;
        this.orderShippedKafkaTemplate = orderShippedKafkaTemplate;
        this.orderDeliveredKafkaTemplate = orderDeliveredKafkaTemplate;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        orderCreatedKafkaTemplate.send("order-events", event);
    }

    public void sendOrderConfirmedEvent(OrderConfirmedEvent event) {
        orderConfirmedKafkaTemplate.send("order-confirmed-events", event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        orderCancelledKafkaTemplate.send("order-cancelled-events", event);
    }

    public void sendOrderShippedEvent(OrderShippedEvent event) {
        orderShippedKafkaTemplate.send("order-shipped-events", event);
    }

    public void sendOrderDeliveredEvent(OrderDeliveredEvent event) {
        orderDeliveredKafkaTemplate.send("order-delivered-events", event);
    }
}
