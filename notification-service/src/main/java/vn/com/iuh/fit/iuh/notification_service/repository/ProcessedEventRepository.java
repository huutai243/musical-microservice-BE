package vn.com.iuh.fit.iuh.notification_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.com.iuh.fit.iuh.notification_service.entity.ProcessedEvent;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {
}
