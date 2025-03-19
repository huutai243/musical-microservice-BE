package vn.com.iuh.fit.order_service.service.impl;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.entity.OrderItem;
import vn.com.iuh.fit.order_service.event.ValidateInventoryEvent;
import vn.com.iuh.fit.order_service.repository.OrderRepository;
import vn.com.iuh.fit.order_service.repository.OrderItemRepository;
import vn.com.iuh.fit.order_service.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Nhận checkout từ Kafka và tạo đơn hàng
     */
    @Override
    public void createOrderFromCheckout(CheckoutEventDTO checkoutEvent) {
        log.info(" Nhận Checkout Event từ Kafka: {}", checkoutEvent);

        try {
            // **1. Lưu Order vào DB**
            Order savedOrder = orderRepository.save(Order.builder()
                    .userId(checkoutEvent.getUserId())
                    .totalPrice(checkoutEvent.getTotalPrice())
                    .status("VALIDATING_INVENTORY")
                    .createdAt(LocalDateTime.now())
                    .build()
            );

            final Long orderId = savedOrder.getId();
            log.info(" Đơn hàng đã lưu vào DB với OrderID: {}", orderId);

            // **2. Lưu Order Items vào DB**
            List<OrderItem> orderItems = checkoutEvent.getItems().stream().map(item ->
                    OrderItem.builder()
                            .order(savedOrder)
                            .productId(item.getProductId())
                            .name(item.getName())
                            .price(item.getPrice())
                            .quantity(item.getRequestedQuantity())
                            .imageUrl(item.getImageUrl())
                            .build()
            ).collect(Collectors.toList());

            try {
                orderItemRepository.saveAll(orderItems);
                log.info(" OrderItem đã lưu vào DB với {} sản phẩm.", orderItems.size());
            } catch (Exception ex) {
                log.error(" Lỗi khi lưu OrderItem vào DB: ", ex);
                throw new RuntimeException("Lỗi khi lưu OrderItem", ex);
            }

            ValidateInventoryEvent inventoryEvent = new ValidateInventoryEvent(
                    orderId,
                    checkoutEvent.getUserId(),
                    orderItems.stream().map(item -> new ValidateInventoryEvent.Item(
                            item.getProductId(),
                            item.getQuantity()
                    )).collect(Collectors.toList())
            );

            // Gửi Kafka event async
            try {
                kafkaTemplate.send("inventory-validation-events", inventoryEvent).get(5, TimeUnit.SECONDS);
                log.info("🚀 Đã gửi sự kiện kiểm tra tồn kho cho Order ID: {}", orderId);
            } catch (Exception ex) {
                log.error("❌ Lỗi khi gửi sự kiện Kafka: ", ex);
            }
        } catch (Exception e) {
            log.error(" Lỗi khi xử lý Checkout Event: ", e);
            throw new RuntimeException("Giao dịch thất bại", e);
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng sau khi xác thực tồn kho
     */
    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setStatus(status);
        orderRepository.save(order);

        log.info(" Cập nhật trạng thái đơn hàng #{} thành {}", orderId, status);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        updateOrderStatus(orderId, "CONFIRMED");
        kafkaTemplate.send("order-confirmed-events", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        updateOrderStatus(orderId, "CANCELLED");
        kafkaTemplate.send("order-cancelled-events", orderId);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        updateOrderStatus(orderId, "SHIPPED");
        kafkaTemplate.send("order-shipped-events", orderId);
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        updateOrderStatus(orderId, "DELIVERED");
        kafkaTemplate.send("order-delivered-events", orderId);
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
