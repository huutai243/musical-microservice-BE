package vn.com.iuh.fit.cart_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

