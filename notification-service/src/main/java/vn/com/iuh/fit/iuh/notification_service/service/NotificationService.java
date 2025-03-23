package vn.com.iuh.fit.iuh.notification_service.service;


import vn.com.iuh.fit.iuh.notification_service.event.NotificationOrderEvent;

public interface NotificationService {
    void handleNotification(NotificationOrderEvent event);
}