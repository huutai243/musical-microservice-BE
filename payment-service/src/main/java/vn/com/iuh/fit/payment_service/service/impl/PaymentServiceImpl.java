package vn.com.iuh.fit.payment_service.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;
import vn.com.iuh.fit.payment_service.entity.Payment;
import vn.com.iuh.fit.payment_service.enums.PaymentStatus;
import vn.com.iuh.fit.payment_service.event.PaymentResultEvent;
import vn.com.iuh.fit.payment_service.gateway.PayPalPaymentGateway;
import vn.com.iuh.fit.payment_service.gateway.PaymentGateway;
import vn.com.iuh.fit.payment_service.gateway.StripePaymentGateway;
import vn.com.iuh.fit.payment_service.producer.PaymentProducer;
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
    @Autowired private PaymentProducer paymentProducer;


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
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        Payment payment = Payment.builder()
                .orderId(paymentRequest.getOrderId())
                .amount(paymentRequest.getAmount())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        paymentProducer.sendPaymentResultEvent(new PaymentResultEvent(
                payment.getOrderId(),
                success,
                success ? " Thanh toán thành công" : " Thanh toán thất bại")
        );

        return payment;
    }

    @Override
    @Transactional
    public void processRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException(" Không tìm thấy giao dịch cần hoàn tiền"));

        if (!PaymentStatus.SUCCESS.name().equals(payment.getStatus())) {
            throw new IllegalStateException(" Chỉ có thể hoàn tiền cho các giao dịch thành công!");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        kafkaTemplate.send("payment-events",
                new PaymentResultEvent(payment.getOrderId(), true, " Hoàn tiền thành công!"));

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
