package vn.com.iuh.fit.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InternalPaymentRequestDTO {
    private Long orderId;
    private Double amount;
    private String paymentMethod;
    private String userId;
}

