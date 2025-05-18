package vn.com.iuh.fit.order_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.dto.OrderItemResponseDTO;
import vn.com.iuh.fit.order_service.dto.OrderResponseDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.entity.OrderItem;
import vn.com.iuh.fit.order_service.entity.OutboxEvent;
import vn.com.iuh.fit.order_service.entity.ProcessedEvent;
import vn.com.iuh.fit.order_service.enums.OrderItemStatus;
import vn.com.iuh.fit.order_service.enums.OrderStatus;
import vn.com.iuh.fit.order_service.event.*;
import vn.com.iuh.fit.order_service.mapper.OrderMapper;
import vn.com.iuh.fit.order_service.producer.OrderProducer;
import vn.com.iuh.fit.order_service.repository.OrderItemRepository;
import vn.com.iuh.fit.order_service.repository.OrderRepository;
import vn.com.iuh.fit.order_service.repository.OutboxEventRepository;
import vn.com.iuh.fit.order_service.repository.ProcessedEventRepository;
import vn.com.iuh.fit.order_service.service.OrderService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderProducer orderProducer;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    @Autowired
    private OrderMapper orderMapper;


    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            OrderProducer orderProducer,
                            ProcessedEventRepository processedEventRepository,
                            ObjectMapper objectMapper,
                            OutboxEventRepository outboxEventRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderProducer = orderProducer;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }

    /**
     * Nhận checkout từ Kafka và tạo đơn hàng
     */
    @Override
    @Transactional
    public void createOrderFromCheckout(CheckoutEventDTO checkoutEvent) {
        log.info(" Nhận Checkout Event từ Kafka: {}", checkoutEvent);
         // Nếu đơn hàng đã được xử lí thì bỏ qua
        if (processedEventRepository.existsById(checkoutEvent.getEventId())) {
            log.warn("CheckoutEvent đã xử lý trước đó! eventId = {}", checkoutEvent.getEventId());
            return;
        }

        //Lưu eventId để mark là đã xử lý
        processedEventRepository.save(
                new ProcessedEvent(
                        checkoutEvent.getEventId(),
                        checkoutEvent.getCorrelationId(),
                        LocalDateTime.now()
                )
        );

        // Đơn hàng với trạng thái "PENDING_INVENTORY_VALIDATION"
        Order savedOrder = orderRepository.save(
                Order.builder()
                        .userId(checkoutEvent.getUserId())
                        .correlationId(checkoutEvent.getCorrelationId())
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
        try {
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

            String payload = objectMapper.writeValueAsString(inventoryEvent);

            outboxEventRepository.save(
                    OutboxEvent.builder()
                            .id(UUID.randomUUID())
                            .aggregateType("Order")
                            .aggregateId(String.valueOf(orderId))
                            .type("ValidateInventoryEvent")
                            .payload(payload)
                            .status("PENDING")
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            log.info(" Ghi sự kiện kiểm tra tồn kho vào Outbox thành công cho Order #{}", orderId);

        } catch (Exception e) {
            log.error(" Lỗi serialize hoặc lưu Outbox ValidateInventoryEvent", e);
            throw new RuntimeException("Lỗi tạo sự kiện tồn kho từ đơn hàng", e);
        }

//        orderProducer.sendInventoryValidationEvent(inventoryEvent);
    }

    @Override
    @Transactional
    public void handleInventoryValidationResult(InventoryValidationResultEvent event) {
        log.info(" Xử lý kết quả kiểm tra tồn kho cho đơn hàng #{}", event.getOrderId());

        //Idempotency
        String eventId = "INVENTORY_RESULT_" + event.getOrderId();

        if (processedEventRepository.existsById(eventId)) {
            log.warn("InventoryValidationResultEvent đã xử lý trước đó! eventId = {}", eventId);
            return;
        }

        processedEventRepository.save(new ProcessedEvent(
                eventId,
                null,
                LocalDateTime.now()
        ));

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

//    @Override
//    @Transactional
//    public void handlePaymentResult(PaymentResultEvent event) {
//        log.info("Xử lý kết quả thanh toán cho đơn hàng #{}", event.getOrderId());
//       // Idempotency
//        String eventId = "PAYMENT_RESULT_" + event.getOrderId();
//
//        // Idempotency check
//        if (processedEventRepository.existsById(eventId)) {
//            log.warn("PaymentResultEvent đã xử lý trước đó! eventId = {}", eventId);
//            return;
//        }
//
//        // Save to mark processed
//        processedEventRepository.save(new ProcessedEvent(
//                eventId,
//                null, // No correlationId
//                LocalDateTime.now()
//        ));
//
//        Order order = orderRepository.findById(event.getOrderId())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + event.getOrderId()));
//
//        if (event.isSuccess()) {
//            order.setStatus(OrderStatus.PAYMENT_SUCCESS);
//            log.info("Đơn hàng #{} thanh toán thành công. Chuyển sang PAYMENT_SUCCESS.", order.getId());
//
//            // --- 1. Tạo sự kiện trừ kho ---
//            InventoryDeductionRequestEvent inventoryEvent = InventoryDeductionRequestEvent.builder()
//                    .orderId(order.getId())
//                    .userId(order.getUserId())
//                    .reason("ORDER_PAID")
//                    .products(order.getItems().stream()
//                            .map(item -> InventoryDeductionRequestEvent.ProductQuantity.builder()
//                                    .productId(item.getProductId())
//                                    .quantity(item.getQuantity())
//                                    .build())
//                            .collect(Collectors.toList()))
//                    .build();
//
//            // --- 2. Tạo sự kiện gửi thông báo ---
//            NotificationOrderEvent notificationEvent = NotificationOrderEvent.builder()
//                    .userId(order.getUserId())
//                    .orderId(order.getId())
//                    .status(OrderStatus.PAYMENT_SUCCESS.name())
//                    .title("Đặt hàng thành công!")
//                    .message("Đơn hàng #" + order.getId() + " đã được thanh toán thành công với tổng tiền " +
//                            order.getTotalPrice() + " VNĐ. Phương thức thanh toán: " + event.getPaymentMethod())
//                    .paymentMethod(event.getPaymentMethod())
//                    .totalAmount(order.getTotalPrice())
//                    .timestamp(Instant.now())
//                    .build();
//
//            try {
//                // --- 3. Ghi vào OutboxEvent ---
//                String inventoryPayload = objectMapper.writeValueAsString(inventoryEvent);
//                String notificationPayload = objectMapper.writeValueAsString(notificationEvent);
//
//                outboxEventRepository.save(
//                        OutboxEvent.builder()
//                                .id(UUID.randomUUID())
//                                .aggregateType("Order")
//                                .aggregateId(String.valueOf(order.getId()))
//                                .type("InventoryDeductionEvent")
//                                .payload(inventoryPayload)
//                                .status("PENDING")
//                                .createdAt(LocalDateTime.now())
//                                .build()
//                );
//
//                outboxEventRepository.save(
//                        OutboxEvent.builder()
//                                .id(UUID.randomUUID())
//                                .aggregateType("Order")
//                                .aggregateId(String.valueOf(order.getId()))
//                                .type("NotificationEvent")
//                                .payload(notificationPayload)
//                                .status("PENDING")
//                                .createdAt(LocalDateTime.now())
//                                .build()
//                );
//
//                log.info(" Ghi sự kiện vào Outbox thành công cho Order #{}", order.getId());
//
//            } catch (Exception ex) {
//                log.error(" Lỗi ghi OutboxEvent: {}", ex.getMessage(), ex);
//                throw new RuntimeException("Không thể serialize/gửi sự kiện hậu thanh toán", ex);
//            }
//
//        } else {
//            order.setStatus(OrderStatus.PAYMENT_FAILED);
//            log.info("Đơn hàng #{} thanh toán thất bại. Chuyển sang PAYMENT_FAILED.", order.getId());
//        }
//
//        orderRepository.save(order);
//    }

    @Override
    @Transactional
    public void handlePaymentResult(PaymentResultEvent event) {
        log.info("Xử lý kết quả thanh toán cho đơn hàng #{}", event.getOrderId());
        String eventId = "PAYMENT_RESULT_" + event.getOrderId();

        if (processedEventRepository.existsById(eventId)) {
            log.warn("PaymentResultEvent đã xử lý trước đó! eventId = {}", eventId);
            return;
        }

        processedEventRepository.save(new ProcessedEvent(
                eventId, null, LocalDateTime.now()
        ));

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + event.getOrderId()));

        if (event.isSuccess()) {
            order.setStatus(OrderStatus.PAYMENT_SUCCESS);
            log.info("Đơn hàng #{} thanh toán thành công. Chuyển sang PAYMENT_SUCCESS.", order.getId());

            InventoryDeductionRequestEvent inventoryEvent = InventoryDeductionRequestEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .reason("ORDER_PAID")
                    .products(order.getItems().stream()
                            .map(item -> InventoryDeductionRequestEvent.ProductQuantity.builder()
                                    .productId(item.getProductId())
                                    .quantity(item.getQuantity())
                                    .build())
                            .toList())
                    .build();

            NotificationOrderEvent notificationEvent = NotificationOrderEvent.builder()
                    .userId(order.getUserId())
                    .orderId(order.getId())
                    .status(OrderStatus.PAYMENT_SUCCESS.name())
                    .title("Đặt hàng thành công!")
                    .message("Đơn hàng #" + order.getId() + " đã được thanh toán thành công với tổng tiền " +
                            order.getTotalPrice() + " VNĐ. Phương thức thanh toán: " + event.getPaymentMethod())
                    .paymentMethod(event.getPaymentMethod())
                    .totalAmount(order.getTotalPrice())
                    .timestamp(Instant.now())
                    .build();

            try {
                String inventoryPayload = objectMapper.writeValueAsString(inventoryEvent);
                String notificationPayload = objectMapper.writeValueAsString(notificationEvent);

                outboxEventRepository.save(OutboxEvent.builder()
                        .id(UUID.randomUUID())
                        .aggregateType("Order")
                        .aggregateId(String.valueOf(order.getId()))
                        .type("InventoryDeductionEvent")
                        .payload(inventoryPayload)
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build());

                outboxEventRepository.save(OutboxEvent.builder()
                        .id(UUID.randomUUID())
                        .aggregateType("Order")
                        .aggregateId(String.valueOf(order.getId()))
                        .type("NotificationEvent")
                        .payload(notificationPayload)
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build());

                log.info("Ghi sự kiện vào Outbox thành công cho Order #{}", order.getId());

            } catch (Exception ex) {
                log.error("Lỗi ghi OutboxEvent: {}", ex.getMessage(), ex);

                try {
                    RefundRequestEvent refundEvent = RefundRequestEvent.builder()
                            .orderId(order.getId())
                            .userId(order.getUserId())
                            .amount(order.getTotalPrice())
                            .paymentMethod(event.getPaymentMethod())
                            .reason("OUTBOX_FAILED")
                            .timestamp(Instant.now())
                            .build();

                    String refundPayload = objectMapper.writeValueAsString(refundEvent);

                    outboxEventRepository.save(OutboxEvent.builder()
                            .id(UUID.randomUUID())
                            .aggregateType("Order")
                            .aggregateId(String.valueOf(order.getId()))
                            .type("RefundRequestEvent")
                            .payload(refundPayload)
                            .status("PENDING")
                            .createdAt(LocalDateTime.now())
                            .build());

                    log.warn("Đã ghi RefundRequestEvent do lỗi ghi Outbox.");

                } catch (Exception fallbackEx) {
                    log.error("Lỗi ghi RefundRequestEvent: {}", fallbackEx.getMessage(), fallbackEx);
                }

                order.setStatus(OrderStatus.PAYMENT_FAILED);
            }

        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            log.info("Đơn hàng #{} thanh toán thất bại. Chuyển sang PAYMENT_FAILED.", order.getId());
        }

        orderRepository.save(order);
    }


    @Override
    @Transactional
    public void removeItemFromOrder(Long orderId, Long itemId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm trong đơn hàng này.");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món con #" + itemId + " trong đơn hàng."));

        if (item.getStatus() != OrderItemStatus.OUT_OF_STOCK) {
            throw new RuntimeException("Chỉ được xóa sản phẩm có trạng thái OUT_OF_STOCK.");
        }

        order.getItems().remove(item);
        orderItemRepository.delete(item);

        log.info("Đã xóa item #{} khỏi order #{}", itemId, orderId);
        if (order.getItems().isEmpty()) {
            order.setStatus(OrderStatus.CANCELLED);
        } else if (order.getItems().stream().allMatch(i -> i.getStatus() == OrderItemStatus.CONFIRMED)) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_CONFIRMED);
        }

        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDTOById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getImageUrl(),
                        item.getStatus()
                ))
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getTotalPrice(),
                order.getUserId(),
                order.getStatus(),
                itemDTOs
        );
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

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy đơn hàng với ID: " + orderId));
    }
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDTO)
                .toList();
    }


}
