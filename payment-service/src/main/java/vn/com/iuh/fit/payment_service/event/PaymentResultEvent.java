package vn.com.iuh.fit.payment_service.event;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultEvent {
    private Long orderId;
    private String userId;
    private Double amount;
    private String paymentMethod;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
}

