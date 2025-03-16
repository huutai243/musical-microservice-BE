package vn.com.iuh.fit.cart_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.cart_service.config.JwtAuthFilter;
import vn.com.iuh.fit.cart_service.dto.CartItemDTO;
import vn.com.iuh.fit.cart_service.event.CheckoutEvent;
import vn.com.iuh.fit.cart_service.service.CartService;

import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * API thêm sản phẩm vào giỏ hàng
     * @param authHeader - JWT token để lấy userId
     * @param guestId - ID khách nếu chưa đăng nhập
     * @param productId - ID sản phẩm
     * @param requestedQuantity - Số lượng cần thêm
     * @return ResponseEntity<String>
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> addToCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "guestId", required = false) String guestId,
            @RequestParam String productId,
            @RequestParam int requestedQuantity) throws Exception {

        String userId = JwtAuthFilter.extractUserId(authHeader);
        String cartOwnerId = (userId != null) ? userId : guestId;

        if (cartOwnerId == null) {
            return ResponseEntity.badRequest().body("Bạn cần đăng nhập hoặc có Guest ID.");
        }

        cartService.addItem(cartOwnerId, productId, requestedQuantity);
        return ResponseEntity.ok("Sản phẩm đã thêm vào giỏ hàng.");
    }

    /**
     * API hợp nhất giỏ hàng của khách với tài khoản đã đăng nhập
     * @param userId - ID người dùng
     * @param guestId - ID khách
     * @return ResponseEntity<String>
     */
    @PostMapping("/merge-cart")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> mergeCart(@RequestParam String userId,
                                            @RequestParam String guestId) throws Exception {
        cartService.mergeGuestCartToUserCart(guestId, userId);
        return ResponseEntity.ok("Giỏ hàng của khách đã được hợp nhất với tài khoản.");
    }

    /**
     * API lấy danh sách sản phẩm trong giỏ hàng
     * @param userId - ID người dùng
     * @return List<CartItemDTO>
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CartItemDTO>> getCart(@PathVariable String userId) throws Exception {
        List<CartItemDTO> cartItems = cartService.getCart(userId);

        if (cartItems.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cartItems);
    }

    /**
     * API xóa một sản phẩm khỏi giỏ hàng
     * @param userId - ID người dùng
     * @param productId - ID sản phẩm cần xóa
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> removeFromCart(@RequestParam String userId, @RequestParam String productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok("Sản phẩm đã được xóa khỏi giỏ hàng");
    }

    /**
     * API xóa toàn bộ giỏ hàng
     * @param userId - ID người dùng
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/clear/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Đã xóa toàn bộ trong giỏ hàng");
    }

    /**
     * API checkout giỏ hàng và gửi sự kiện qua Kafka
     * @param authHeader - JWT token của người dùng
     * @return CheckoutEvent
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String userId = JwtAuthFilter.extractUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Bạn cần đăng nhập để thanh toán!");
            }

            CheckoutEvent event = cartService.checkout(userId);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
