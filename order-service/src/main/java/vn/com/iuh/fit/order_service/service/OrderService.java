package vn.com.iuh.fit.order_service.service;

import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.entity.Order;
import vn.com.iuh.fit.order_service.enums.OrderStatus;
import vn.com.iuh.fit.order_service.event.InventoryValidationResultEvent;

import java.util.List;

public interface OrderService {
    void handleInventoryValidationResult(InventoryValidationResultEvent event);
    void createOrderFromCheckout(CheckoutEventDTO checkoutEvent);
    void confirmOrder(Long orderId);
    void cancelOrder(Long orderId);
    void shipOrder(Long orderId);
    void deliverOrder(Long orderId);
    void updateAndPublishStatus(Long orderId, OrderStatus status, String topic);
    List<Order> getAllOrders();
    Order getOrderById(Long orderId);
}
