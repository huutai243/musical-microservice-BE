package vn.com.iuh.fit.iuh.notification_service.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
