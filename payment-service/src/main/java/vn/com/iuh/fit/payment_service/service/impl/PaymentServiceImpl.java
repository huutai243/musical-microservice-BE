package vn.com.iuh.fit.payment_service.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;
import vn.com.iuh.fit.payment_service.entity.Payment;
import vn.com.iuh.fit.payment_service.event.PaymentConfirmedEvent;
import vn.com.iuh.fit.payment_service.gateway.*;
import vn.com.iuh.fit.payment_service.repository.PaymentRepository;
import vn.com.iuh.fit.payment_service.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = Logger.getLogger(PaymentServiceImpl.class.getName());

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private StripePaymentGateway stripePaymentGateway;
    @Autowired private PayPalPaymentGateway paypalPaymentGateway;

    @Override
    @Transactional
    public Payment processPayment(PaymentRequestDTO paymentRequest) {
        log.info(" Xử lý thanh toán bằng phương thức: " + paymentRequest.getPaymentMethod());

        PaymentGateway gateway;
        switch (paymentRequest.getPaymentMethod().toUpperCase()) {
            case "STRIPE":
                gateway = stripePaymentGateway;
                break;
            case "PAYPAL":
                gateway = paypalPaymentGateway;
                break;
            default:
                throw new IllegalArgumentException(" Phương thức thanh toán không hợp lệ!");
        }

        boolean success = gateway.processPayment(paymentRequest);
        String status = success ? "SUCCESS" : "FAILED";

        Payment payment = Payment.builder()
                .orderId(paymentRequest.getOrderId())
                .userId(paymentRequest.getUserId())
                .amount(paymentRequest.getAmount())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        kafkaTemplate.executeInTransaction(kafka -> {
            kafka.send("payment-events", new PaymentConfirmedEvent(payment.getId(), payment.getOrderId(), payment.getUserId(), status));
            return null;
        });

        return payment;
    }

    @Override
    @Transactional
    public void processRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy giao dịch cần hoàn tiền"));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new IllegalStateException(" Chỉ có thể hoàn tiền cho các giao dịch thành công!");
        }

        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);

        kafkaTemplate.executeInTransaction(kafka -> {
            kafka.send("payment-events", new PaymentConfirmedEvent(payment.getId(), payment.getOrderId(), payment.getUserId(), "REFUNDED"));
            return null;
        });

        log.info(" Hoàn tiền thành công cho Payment ID: " + paymentId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy thanh toán với ID: " + paymentId));
    }
}
