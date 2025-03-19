package vn.com.iuh.fit.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.order_service.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
