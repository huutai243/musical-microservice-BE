package vn.com.iuh.fit.payment_service.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class PaymentRequestDTO {
    @NotNull
    private Long orderId;

    @NotBlank
    private String paymentMethod; // STRIPE, VNPAY, MOMO, PAYPAL
}
