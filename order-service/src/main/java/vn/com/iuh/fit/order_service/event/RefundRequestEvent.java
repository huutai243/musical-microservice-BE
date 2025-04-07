package vn.com.iuh.fit.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequestEvent {
    private Long orderId;
    private String userId;
    private Double amount;
    private String paymentMethod;
    private String reason;
    private Instant timestamp;
}

