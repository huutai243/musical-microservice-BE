package vn.com.iuh.fit.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundRequestEvent {
    private Long orderId;
    private String userId;
    private Double amount;
    private String reason;
    private Instant timestamp;
}
