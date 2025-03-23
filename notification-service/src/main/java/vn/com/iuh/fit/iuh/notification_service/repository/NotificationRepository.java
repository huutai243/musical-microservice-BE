package vn.com.iuh.fit.iuh.notification_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.com.iuh.fit.iuh.notification_service.entity.Notification;


import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
}