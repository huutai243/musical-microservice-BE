package vn.com.iuh.fit.iuh.notification_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    private String userId;
    private Long orderId;
    private String status;
    private String title;
    private String message;
    private String paymentMethod;
    private Double totalAmount;
    private Instant timestamp;

    private boolean isRead = false;
}
