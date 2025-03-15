package vn.com.iuh.fit.payment_service.service;

import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;
import vn.com.iuh.fit.payment_service.entity.Payment;

import java.util.List;

public interface PaymentService {
    Payment processPayment(PaymentRequestDTO paymentRequest);
    void processRefund(Long paymentId);
    List<Payment> getAllPayments();
    Payment getPaymentById(Long paymentId);
}
