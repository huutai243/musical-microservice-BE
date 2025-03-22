package vn.com.iuh.fit.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.order_service.config.JwtAuthFilter;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.dto.OrderResponseDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.service.OrderService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * API tạo đơn hàng
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<Void> createOrder(@RequestBody CheckoutEventDTO checkoutEvent) {
        orderService.createOrderFromCheckout(checkoutEvent);
        return ResponseEntity.ok().build();
    }

    /**
     * API lấy danh sách tất cả đơn hàng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * API lấy đơn hàng theo ID
     */
    @GetMapping("/get/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String userId = JwtAuthFilter.extractUserId(authHeader, null);
        Order order = orderService.getOrderById(orderId);

        if (userId.equals(order.getUserId())) {
            return ResponseEntity.ok(order);
        }

        return ResponseEntity.status(403).build();
    }

    @GetMapping("/internal/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderInternalById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(convertToDTO(order)); // Trả về DTO gọn
    }

    /**
     * API xác nhận đơn hàng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirmOrder(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * API hủy đơn hàng
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId, Principal principal) {
        Order order = orderService.getOrderById(orderId);
        if (principal.getName().equals(order.getUserId()) || principal.getName().equals("ADMIN")) {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(403).build();
    }

    /**
     * API giao đơn hàng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/ship/{orderId}")
    public ResponseEntity<Void> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * API hoàn tất giao hàng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deliver/{orderId}")
    public ResponseEntity<Void> deliverOrder(@PathVariable Long orderId) {
        orderService.deliverOrder(orderId);
        return ResponseEntity.ok().build();
    }
    private OrderResponseDTO convertToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getTotalPrice(),
                order.getUserId(),
                order.getStatus()
        );
    }

}
