package vn.com.iuh.fit.order_service.service.impl;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.event.*;
import vn.com.iuh.fit.order_service.producer.OrderProducer;
import vn.com.iuh.fit.order_service.repository.OrderRepository;
import vn.com.iuh.fit.order_service.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public Order createOrder(String userId, String productId, Integer quantity, Double price) {
        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        try {
            kafkaTemplate.executeInTransaction(kafka -> {
                kafka.send("order-events", new OrderCreatedEvent(order.getId(), userId, productId, quantity, price, "PENDING"));
                return null;
            });
        } catch (Exception e) {
            log.error("Lỗi khi gửi sự kiện Kafka! Rollback đơn hàng...");
            throw new RuntimeException("Lỗi Kafka, rollback đơn hàng!");
        }

        return order;
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus("CONFIRMED");
        orderRepository.save(order);

        kafkaTemplate.executeInTransaction(kafka -> {
            kafka.send("order-confirmed-events", new OrderConfirmedEvent(order.getId(), order.getUserId(), "Đơn hàng #" + orderId + " đã được xác nhận."));
            kafka.send("notification-events", new NotificationEvent(order.getUserId(), "Đơn hàng #" + orderId + " đã được xác nhận!"));
            return null;
        });

        log.info(" Đơn hàng #{} đã được xác nhận.", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        kafkaTemplate.executeInTransaction(kafka -> {
            kafka.send("order-cancelled-events", new OrderCancelledEvent(order.getId(), order.getUserId(), "Đơn hàng đã bị hủy."));
            return null;
        });
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus("SHIPPED");
        orderRepository.save(order);

        kafkaTemplate.executeInTransaction(kafka -> {
            kafka.send("order-shipped-events", new OrderShippedEvent(order.getId(), order.getUserId(), "Đơn hàng đã được giao."));
            return null;
        });
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus("DELIVERED");
        orderRepository.save(order);

        kafkaTemplate.executeInTransaction(kafka -> {
            kafka.send("order-delivered-events", new OrderDeliveredEvent(order.getId(), order.getUserId(), "Đơn hàng đã đến tay khách hàng."));
            return null;
        });
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
    }
}
