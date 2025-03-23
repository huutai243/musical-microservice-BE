package vn.com.iuh.fit.payment_service.gateway;

import vn.com.iuh.fit.payment_service.dto.InternalPaymentRequestDTO;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;

public interface PaymentGateway {
    boolean processPayment(InternalPaymentRequestDTO paymentRequest);
    String generatePaymentUrl(InternalPaymentRequestDTO paymentRequest);
}
