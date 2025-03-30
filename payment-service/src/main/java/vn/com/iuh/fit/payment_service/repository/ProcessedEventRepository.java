package vn.com.iuh.fit.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.payment_service.entity.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
