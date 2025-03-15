package vn.com.iuh.fit.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedEvent {
    private Long paymentId;
    private Long orderId;
    private String userId;
    private String status;
}
