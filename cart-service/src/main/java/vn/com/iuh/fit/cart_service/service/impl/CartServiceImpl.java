package vn.com.iuh.fit.cart_service.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.com.iuh.fit.cart_service.client.ProductClient;
import vn.com.iuh.fit.cart_service.dto.CartItemDTO;
import vn.com.iuh.fit.cart_service.dto.ProductDTO;
import vn.com.iuh.fit.cart_service.entity.CartItem;
import vn.com.iuh.fit.cart_service.event.CheckoutEvent;
import vn.com.iuh.fit.cart_service.producer.CartProducer;
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
    private final CartProducer cartProducer;


    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductClient productClient, StringRedisTemplate redisTemplate, ObjectMapper objectMapper, CartProducer cartProducer) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.cartProducer = cartProducer;
    }

    @Override
    public void addItem(String userId, String productId, int requestedQuantity) throws Exception {
        String key = "cart:" + userId;
        Object existingItemJson = redisTemplate.opsForHash().get(key, productId);

        if (existingItemJson != null) {
            CartItem cartItem = objectMapper.readValue(existingItemJson.toString(), CartItem.class);
            int newQuantity = cartItem.getRequestedQuantity() + requestedQuantity;
            if (newQuantity <= 0) {
                redisTemplate.opsForHash().delete(key, productId);
                return;
            }
            cartItem.setRequestedQuantity(newQuantity);
            String updatedCartItemJson = objectMapper.writeValueAsString(cartItem);
            redisTemplate.opsForHash().put(key, productId, updatedCartItemJson);
        } else {
            if (requestedQuantity <= 0) {
                return;
            }
            ProductDTO product = productClient.getProductById(productId);
            CartItem cartItem = new CartItem(
                    productId,
                    product.getName(),
                    product.getPrice(),
                    requestedQuantity,
                    product.getFirstImageUrl()
            );
            String cartItemJson = objectMapper.writeValueAsString(cartItem);
            redisTemplate.opsForHash().put(key, productId, cartItemJson);
        }
    }


    @Override
    public List<CartItemDTO> getCart(String userId) throws Exception {
        String key = "cart:" + userId;

        return redisTemplate.opsForHash().values(key).stream()
                .filter(json -> json instanceof String)
                .map(json -> {
                    try {
                        CartItemDTO item = objectMapper.readValue((String) json, CartItemDTO.class);
                        if (item.getImageUrl() == null) {
                            ProductDTO product = productClient.getProductById(item.getProductId());
                            item.setImageUrl(product.getFirstImageUrl());
                        }

                        return item;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    public void removeItem(String userId, String productId) {
        String key = "cart:" + userId;
//        String productKey = String.valueOf(productId);
//        redisTemplate.opsForHash().delete(key, productKey);
        redisTemplate.opsForHash().delete(key, productId);
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

    @Override
    public CheckoutEvent checkout(String userId) throws Exception {
        String key = "cart:" + userId;
        List<CartItemDTO> cartItems = redisTemplate.opsForHash().values(key).stream()
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

        if (cartItems.isEmpty()) {
            throw new Exception("Giỏ hàng trống!");
        }
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getRequestedQuantity())
                .sum();
        CheckoutEvent event = new CheckoutEvent(userId, cartItems, totalPrice);
        cartProducer.sendCheckoutEvent(event);
        redisTemplate.delete(key);
        return event;
    }
}
