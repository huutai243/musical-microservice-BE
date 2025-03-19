package vn.com.iuh.fit.order_service.service;

import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.entity.Order;

import java.util.List;

public interface OrderService {
    void createOrderFromCheckout(CheckoutEventDTO checkoutEvent);
    void updateOrderStatus(Long orderId, String status);
    void confirmOrder(Long orderId);
    void cancelOrder(Long orderId);
    void shipOrder(Long orderId);
    void deliverOrder(Long orderId);
    List<Order> getAllOrders();
    Order getOrderById(Long orderId);
}
