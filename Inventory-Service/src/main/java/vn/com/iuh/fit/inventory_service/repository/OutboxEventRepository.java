package vn.com.iuh.fit.inventory_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.inventory_service.entity.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatus(String status);
}

