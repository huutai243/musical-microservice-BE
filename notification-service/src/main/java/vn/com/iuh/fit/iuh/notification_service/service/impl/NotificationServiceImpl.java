package vn.com.iuh.fit.iuh.notification_service.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.iuh.notification_service.client.EmailClient;
import vn.com.iuh.fit.iuh.notification_service.dto.EmailRequest;
import vn.com.iuh.fit.iuh.notification_service.entity.Notification;
import vn.com.iuh.fit.iuh.notification_service.event.NotificationOrderEvent;
import vn.com.iuh.fit.iuh.notification_service.repository.NotificationRepository;
import vn.com.iuh.fit.iuh.notification_service.service.EmailService;
import vn.com.iuh.fit.iuh.notification_service.service.NotificationService;


@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final EmailClient emailClient;
    private final EmailService emailService;
    @Override
    public void handleNotification(NotificationOrderEvent event) {
        // 1. Lưu vào MongoDB
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .orderId(event.getOrderId())
                .status(event.getStatus())
                .title(event.getTitle())
                .message(event.getMessage())
                .paymentMethod(event.getPaymentMethod())
                .totalAmount(event.getTotalAmount())
                .timestamp(event.getTimestamp())
                .build();
        repository.save(notification);
        log.info(" Lưu thông báo thành công cho user {}", event.getUserId());

        try {
            // 2. Gọi auth-service để lấy email
            String emailTo = emailClient.getEmailByUserId(Long.parseLong(event.getUserId()));

            emailService.sendEmail(emailTo, event.getTitle(), event.getMessage());

            log.info("Gửi email thành công đến {}", emailTo);
        } catch (Exception ex) {
            log.error(" Lỗi khi gửi email: {}", ex.getMessage(), ex);
        }
    }
}


