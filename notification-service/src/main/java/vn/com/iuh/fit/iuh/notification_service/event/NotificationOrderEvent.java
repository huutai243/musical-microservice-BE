package vn.com.iuh.fit.iuh.notification_service.event;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationOrderEvent {
    private String userId;
    private Long orderId;
    private String status;
    private String title;
    private String message;
    private String paymentMethod;
    private Double totalAmount;
    private Instant timestamp;
}