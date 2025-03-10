package vn.com.iuh.fit.cart_service.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vn.com.iuh.fit.cart_service.entity.CartItem;
import java.util.*;

@Repository
public class CartRepository {
    private final RedisTemplate<String, CartItem> redisTemplate;
    private HashOperations<String, String, CartItem> hashOperations;

    public CartRepository(RedisTemplate<String, CartItem> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        this.hashOperations = redisTemplate.opsForHash();
    }

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(String userId, CartItem item) {
        hashOperations.put("cart:" + userId, item.getProductId(), item);
    }

    // Lấy giỏ hàng
    public List<CartItem> getCart(String userId) {
        return new ArrayList<>(hashOperations.entries("cart:" + userId).values());
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeItem(String userId, String productId) {
        hashOperations.delete("cart:" + userId, productId);
    }

    // Xóa toàn bộ giỏ hàng của user
    public void clearCart(String userId) {
        redisTemplate.delete("cart:" + userId);
    }

    // Lưu giỏ hàng sau khi hợp nhất Guest → User
    public void saveCart(String userId, List<CartItem> cartItems) {
        clearCart(userId);
        for (CartItem item : cartItems) {
            addToCart(userId, item);
        }
    }
}
