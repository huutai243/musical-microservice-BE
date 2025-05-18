package vn.com.iuh.fit.order_service.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.order_service.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByCorrelationId(String correlationId);
    List<Order> findByUserId(String userId);
}
