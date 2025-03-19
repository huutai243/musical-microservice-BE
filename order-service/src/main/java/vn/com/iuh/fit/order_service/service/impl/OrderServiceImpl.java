package vn.com.iuh.fit.order_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.entity.OrderItem;
import vn.com.iuh.fit.order_service.event.ValidateInventoryEvent;
import vn.com.iuh.fit.order_service.producer.OrderProducer;
import vn.com.iuh.fit.order_service.repository.OrderItemRepository;
import vn.com.iuh.fit.order_service.repository.OrderRepository;
import vn.com.iuh.fit.order_service.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderProducer orderProducer;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderProducer = orderProducer;
    }

    /**
     * Nhận checkout từ Kafka và tạo đơn hàng
     */
    @Override
    @Transactional
    public void createOrderFromCheckout(CheckoutEventDTO checkoutEvent) {
        log.info(" Nhận Checkout Event từ Kafka: {}", checkoutEvent);

        //  Lưu đơn hàng vào DB
        Order savedOrder = orderRepository.save(
                Order.builder()
                        .userId(checkoutEvent.getUserId())
                        .totalPrice(checkoutEvent.getTotalPrice())
                        .status("VALIDATING_INVENTORY")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Long orderId = savedOrder.getId();
        log.info(" Đã lưu Order vào DB với ID: {}", orderId);

        //  Lưu OrderItems vào DB
        List<OrderItem> orderItems = checkoutEvent.getItems().stream()
                .map(item -> OrderItem.builder()
                        .order(savedOrder)
                        .productId(item.getProductId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .quantity(item.getRequestedQuantity())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
        log.info(" Đã lưu {} sản phẩm vào OrderItems.", orderItems.size());

        // Gửi sự kiện kiểm tra tồn kho
        ValidateInventoryEvent inventoryEvent = new ValidateInventoryEvent(
                orderId,
                checkoutEvent.getUserId(),
                orderItems.stream().map(item ->
                                new ValidateInventoryEvent.Item(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );

        orderProducer.sendInventoryValidationEvent(inventoryEvent);
    }

    /**
     * Cập nhật trạng thái đơn hàng & gửi Kafka event
     */
    @Override
    @Transactional
    public void updateAndPublishStatus(Long orderId, String status, String topic) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);

        log.info(" Cập nhật trạng thái Order #{} -> {}", orderId, status);
        orderProducer.sendOrderStatusEvent(topic, orderId);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        updateAndPublishStatus(orderId, "CONFIRMED", "order-confirmed-events");
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        updateAndPublishStatus(orderId, "CANCELLED", "order-cancelled-events");
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        updateAndPublishStatus(orderId, "SHIPPED", "order-shipped-events");
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        updateAndPublishStatus(orderId, "DELIVERED", "order-delivered-events");
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
