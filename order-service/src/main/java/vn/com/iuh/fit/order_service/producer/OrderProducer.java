package vn.com.iuh.fit.order_service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.event.*;

@Service
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, OrderConfirmedEvent> orderConfirmedKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, OrderShippedEvent> orderShippedKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, OrderDeliveredEvent> orderDeliveredKafkaTemplate;

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
