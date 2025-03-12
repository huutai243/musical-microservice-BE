package vn.com.iuh.fit.cart_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.cart_service.config.JwtAuthFilter;
import vn.com.iuh.fit.cart_service.dto.CartItemDTO;
import vn.com.iuh.fit.cart_service.service.CartService;

import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
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

    @PostMapping("/merge-cart")
    public ResponseEntity<String> mergeCart(@RequestParam String userId,
                                            @RequestParam String guestId) throws Exception {
        cartService.mergeGuestCartToUserCart(guestId, userId);
        return ResponseEntity.ok("Giỏ hàng của khách đã được hợp nhất với tài khoản.");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItemDTO>> getCart(@PathVariable String userId) throws Exception {
        List<CartItemDTO> cartItems = cartService.getCart(userId);

        if (cartItems.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cartItems);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromCart(@RequestParam String userId, @RequestParam String productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok("Sản phẩm đã được xóa khỏi giỏ hàng");
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<String> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Đã xóa toàn bộ trong giỏ hàng");
    }
}
