package vn.com.iuh.fit.AuthService.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
