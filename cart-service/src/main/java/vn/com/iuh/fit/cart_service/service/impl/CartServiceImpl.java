package vn.com.iuh.fit.cart_service.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.com.iuh.fit.cart_service.client.ProductClient;
import vn.com.iuh.fit.cart_service.dto.CartItemDTO;
import vn.com.iuh.fit.cart_service.dto.ProductDTO;
import vn.com.iuh.fit.cart_service.entity.CartItem;
import vn.com.iuh.fit.cart_service.repository.CartRepository;
import vn.com.iuh.fit.cart_service.service.CartService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductClient productClient, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addItem(String userId, String productId, int requestedQuantity) throws Exception {
        ProductDTO product = productClient.getProductById(productId);
        CartItem cartItem = new CartItem(productId, product.getName(), product.getPrice(), requestedQuantity, product.getImageUrl());
        String cartItemJson = objectMapper.writeValueAsString(cartItem);
        String key = "cart:" + userId;
        redisTemplate.opsForHash().put(key, productId, cartItemJson);
    }


    @Override
    public List<CartItemDTO> getCart(String userId) throws Exception {
        String key = "cart:" + userId;

        return redisTemplate.opsForHash().values(key).stream()
                .filter(json -> json instanceof String)
                .map(json -> {
                    try {
                        return objectMapper.readValue((String) json, CartItemDTO.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }


    @Override
    public void removeItem(String userId, String productId) {
        String key = "cart:" + userId;
        redisTemplate.opsForHash().delete(key, "productId:" + productId);
    }

    @Override
    public void clearCart(String userId) {
        String key = "cart:" + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void mergeGuestCartToUserCart(String guestId, String userId) throws Exception {
        List<CartItem> guestCart = cartRepository.getCart(guestId);
        List<CartItem> userCart = cartRepository.getCart(userId);

        for (CartItem guestItem : guestCart) {
            Optional<CartItem> existingItem = userCart.stream()
                    .filter(item -> item.getProductId().equals(guestItem.getProductId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setRequestedQuantity(existingItem.get().getRequestedQuantity() + guestItem.getRequestedQuantity());
            } else {
                userCart.add(guestItem);
            }
        }

        cartRepository.saveCart(userId, userCart);
        cartRepository.clearCart(guestId);
    }
}
