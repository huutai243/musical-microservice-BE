package vn.com.iuh.fit.cart_service.service;

import vn.com.iuh.fit.cart_service.dto.CartItemDTO;
import vn.com.iuh.fit.cart_service.event.CheckoutEvent;

import java.util.List;

public interface CartService {
    void addItem(String userId, String productId, int quantity) throws Exception;
    List<CartItemDTO> getCart(String userId) throws Exception;
    void removeItem(String userId, String productId);
    void clearCart(String userId);
    void mergeGuestCartToUserCart(String guestId, String userId) throws Exception;
    CheckoutEvent checkout(String userId) throws Exception;
}
