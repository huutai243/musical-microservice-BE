package vn.com.iuh.fit.order_service.service;

import vn.com.iuh.fit.order_service.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(String userId, String productId, Integer quantity, Double price);
    void confirmOrder(Long orderId);
    void cancelOrder(Long orderId);
    void shipOrder(Long orderId);
    void deliverOrder(Long orderId);
    List<Order> getAllOrders();
    Order getOrderById(Long orderId);
}
