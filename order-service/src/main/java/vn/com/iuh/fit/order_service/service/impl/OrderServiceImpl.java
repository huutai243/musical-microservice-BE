package vn.com.iuh.fit.order_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.entity.OrderItem;
import vn.com.iuh.fit.order_service.enums.OrderItemStatus;
import vn.com.iuh.fit.order_service.enums.OrderStatus;
import vn.com.iuh.fit.order_service.event.InventoryDeductionRequestEvent;
import vn.com.iuh.fit.order_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.order_service.event.PaymentResultEvent;
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

        // Đơn hàng với trạng thái "PENDING_INVENTORY_VALIDATION"
        Order savedOrder = orderRepository.save(
                Order.builder()
                        .userId(checkoutEvent.getUserId())
                        .totalPrice(checkoutEvent.getTotalPrice())
                        .status(OrderStatus.PENDING_INVENTORY_VALIDATION)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Long orderId = savedOrder.getId();
        log.info(" Đã lưu Order vào DB với ID: {}", orderId);

        // Lưu OrderItems vào DB với trạng thái "PENDING"
        List<OrderItem> orderItems = checkoutEvent.getItems().stream()
                .map(item -> OrderItem.builder()
                        .order(savedOrder)
                        .productId(item.getProductId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .quantity(item.getRequestedQuantity())
                        .imageUrl(item.getImageUrl())
                        .status(OrderItemStatus.PENDING) // Mặc định trạng thái là PENDING
                        .build())
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
        log.info(" Đã lưu {} sản phẩm vào OrderItems.", orderItems.size());

        // Gửi sự kiện kiểm tra tồn kho
        ValidateInventoryEvent inventoryEvent = new ValidateInventoryEvent(
                orderId,
                checkoutEvent.getUserId(),
                orderItems.stream()
                        .map(item -> new ValidateInventoryEvent.Item(
                                item.getProductId(),
                                item.getQuantity(),
                                "PENDING"
                        ))
                        .collect(Collectors.toList())
        );

        orderProducer.sendInventoryValidationEvent(inventoryEvent);
    }

    @Override
    @Transactional
    public void handleInventoryValidationResult(InventoryValidationResultEvent event) {
        log.info(" Xử lý kết quả kiểm tra tồn kho cho đơn hàng #{}", event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy đơn hàng #" + event.getOrderId()));

        // Cập nhật trạng thái từng sản phẩm trong đơn hàng
        for (InventoryValidationResultEvent.Item validatedItem : event.getValidatedItems()) {
            order.getItems().stream()
                    .filter(item -> item.getProductId().equals(validatedItem.getProductId()))
                    .forEach(item -> item.setStatus(validatedItem.getStatus().equals("CONFIRMED")
                            ? OrderItemStatus.CONFIRMED
                            : OrderItemStatus.OUT_OF_STOCK));
        }

        // Xác định trạng thái đơn hàng dựa trên kết quả kiểm tra tồn kho
        boolean allItemsConfirmed = order.getItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.CONFIRMED);
        boolean allItemsOutOfStock = order.getItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.OUT_OF_STOCK);

        if (allItemsConfirmed) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            log.info(" Đơn hàng #{} đã được xác nhận tồn kho. Chuyển sang trạng thái PENDING_PAYMENT.", order.getId());

        } else if (allItemsOutOfStock) {
            order.setStatus(OrderStatus.CANCELLED);
            log.info(" Đơn hàng #{} bị hủy do toàn bộ sản phẩm hết hàng.", order.getId());

        } else {
            order.setStatus(OrderStatus.PARTIALLY_CONFIRMED);
            log.info(" Đơn hàng #{} có một số sản phẩm hết hàng. Chuyển sang trạng thái PARTIALLY_CONFIRMED.", order.getId());
        }

        // Lưu thay đổi vào DB
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void handlePaymentResult(PaymentResultEvent event) {
        log.info(" Xử lý kết quả thanh toán cho đơn hàng #{}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy đơn hàng #" + event.getOrderId()));
        if (event.isSuccess()) {
            order.setStatus(OrderStatus.PAYMENT_SUCCESS);
            log.info(" Đơn hàng #{} thanh toán thành công. Chuyển sang trạng thái PAYMENT_SUCCESS.", order.getId());
            InventoryDeductionRequestEvent inventoryEvent = InventoryDeductionRequestEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .reason("ORDER_PAID")
                    .products(order.getItems().stream()
                            .map(item -> InventoryDeductionRequestEvent.ProductQuantity.builder()
                                    .productId(item.getProductId())
                                    .quantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            orderProducer.sendInventoryDeductionRequest(inventoryEvent);
            log.info("Gửi event đến inventory để cập nhật số lượng");
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            log.info(" Đơn hàng #{} thanh toán thất bại. Chuyển sang trạng thái PAYMENT_FAILED.", order.getId());
        }

        orderRepository.save(order);
    }


    /**
     * Cập nhật trạng thái đơn hàng & gửi Kafka event
     */
    @Override
    @Transactional
    public void updateAndPublishStatus(Long orderId, OrderStatus status, String topic) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy đơn hàng với ID: " + orderId));

        order.setStatus(status);

        for (OrderItem item : order.getItems()) {
            if (status == OrderStatus.CONFIRMED) {
                item.setStatus(OrderItemStatus.CONFIRMED);
            } else if (status == OrderStatus.CANCELLED) {
                item.setStatus(OrderItemStatus.CANCELLED);
            } else if (status == OrderStatus.SHIPPED) {
                item.setStatus(OrderItemStatus.SHIPPED);
            } else if (status == OrderStatus.DELIVERED) {
                item.setStatus(OrderItemStatus.DELIVERED);
            }
        }

        orderRepository.save(order);

        log.info(" Cập nhật trạng thái Order #{} -> {}", orderId, status);
        orderProducer.sendOrderStatusEvent(topic, orderId);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        updateAndPublishStatus(orderId, OrderStatus.CONFIRMED, "order-confirmed-events");
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        updateAndPublishStatus(orderId, OrderStatus.CANCELLED, "order-cancelled-events");
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        updateAndPublishStatus(orderId, OrderStatus.SHIPPED, "order-shipped-events");
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        updateAndPublishStatus(orderId, OrderStatus.DELIVERED, "order-delivered-events");
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy đơn hàng với ID: " + orderId));
    }
}
