package vn.com.iuh.fit.payment_service.gateway;

import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;

public interface PaymentGateway {
    boolean processPayment(PaymentRequestDTO paymentRequest);
    String generatePaymentUrl(PaymentRequestDTO paymentRequest);
}
