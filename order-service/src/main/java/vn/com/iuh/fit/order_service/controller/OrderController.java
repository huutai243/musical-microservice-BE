package vn.com.iuh.fit.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.order_service.config.JwtAuthFilter;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.dto.OrderCorrelationResponseDTO;
import vn.com.iuh.fit.order_service.dto.OrderItemResponseDTO;
import vn.com.iuh.fit.order_service.dto.OrderResponseDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.repository.OrderRepository;
import vn.com.iuh.fit.order_service.service.OrderService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * API tạo đơn hàng
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<Void> createOrder(@RequestBody CheckoutEventDTO checkoutEvent) {
        orderService.createOrderFromCheckout(checkoutEvent);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/get-by-correlation/{cid}")
    public ResponseEntity<OrderCorrelationResponseDTO> getOrderByCorrelation(@PathVariable String cid) {
        return orderRepository.findByCorrelationId(cid)
                .map(order -> ResponseEntity.ok(convertToOrderCorrelationDTO(order)))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    /**
     * API lấy danh sách tất cả đơn hàng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    @GetMapping("/user/get-all")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String userId = JwtAuthFilter.extractUserId(authHeader, jwtSecret);
        return ResponseEntity.ok(orderService.getAllOrdersByUserId(userId));
    }


    /**
     * API lấy đơn hàng theo ID
     */
    @GetMapping("/get/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String userId = JwtAuthFilter.extractUserId(authHeader,jwtSecret);
        OrderResponseDTO dto = orderService.getOrderDTOById(orderId);
        if (userId.equals(dto.getUserId())) {
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.status(403).build();
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String userId = JwtAuthFilter.extractUserId(authHeader, jwtSecret);
        orderService.removeItemFromOrder(orderId, itemId, userId);
        return ResponseEntity.ok("Xóa món thành công.");
    }


//    @GetMapping("/internal/{orderId}")
//    public ResponseEntity<OrderResponseDTO> getOrderInternalById(@PathVariable Long orderId) {
//        Order order = orderService.getOrderById(orderId);
//        return ResponseEntity.ok(convertToDTO(order)); // Trả về DTO gọn
//    }
    @GetMapping("/internal/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderInternalById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDTOById(orderId));
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


    private OrderCorrelationResponseDTO convertToOrderCorrelationDTO(Order order) {
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

        return new OrderCorrelationResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCorrelationId(),
                order.getCreatedAt(),
                itemDTOs
        );
    }

}
