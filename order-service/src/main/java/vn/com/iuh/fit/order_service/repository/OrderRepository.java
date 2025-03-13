package vn.com.iuh.fit.order_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.order_service.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
