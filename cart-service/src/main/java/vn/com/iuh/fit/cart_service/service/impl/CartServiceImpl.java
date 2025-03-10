package vn.com.iuh.fit.cart_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate;

    public CartServiceImpl(CartRepository cartRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void addItem(String userId, String productId, int requestedQuantity) throws Exception {
        ProductDTO product = restTemplate.getForObject("http://product-service/products/" + productId, ProductDTO.class);
        CartItem cartItem = new CartItem(productId, product.getName(), product.getPrice(), requestedQuantity, product.getImageUrl());
        cartRepository.addToCart(userId, cartItem);
    }

    @Override
    public List<CartItemDTO> getCart(String userId) throws Exception {
        return cartRepository.getCart(userId).stream()
                .map(item -> new CartItemDTO(item.getProductId(), item.getName(), item.getPrice(), item.getRequestedQuantity(), item.getImageUrl()))
                .collect(Collectors.toList());
    }

    @Override
    public void removeItem(String userId, String productId) {
        cartRepository.removeItem(userId, productId);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.clearCart(userId);
    }

    @Override
    public void mergeGuestCartToUserCart(String guestId, String userId) throws Exception {
        List<CartItem> guestCart = cartRepository.getCart(guestId);
        List<CartItem> userCart = cartRepository.getCart(userId);

        // Hợp nhất giỏ hàng: Nếu sản phẩm đã có trong user cart, tăng số lượng
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
