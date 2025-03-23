package vn.com.iuh.fit.payment_service.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.client.OrderClient;
import vn.com.iuh.fit.payment_service.dto.InternalPaymentRequestDTO;
import vn.com.iuh.fit.payment_service.dto.OrderResponseDTO;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;
import vn.com.iuh.fit.payment_service.entity.Payment;
import vn.com.iuh.fit.payment_service.enums.OrderStatus;
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
    @Autowired private OrderClient orderClient;



    @Override
    @Transactional
    public Payment processPayment(PaymentRequestDTO paymentRequest) {
        log.info("Xử lý thanh toán cho Order #" + paymentRequest.getOrderId());

        // 1. Gọi sang OrderService để lấy thông tin đơn hàng
        OrderResponseDTO order = orderClient.getOrderById(paymentRequest.getOrderId());

        // 2. Kiểm tra trạng thái đơn hàng
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT &&
                order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái cho phép thanh toán!");
        }

        // 3. Lấy số tiền thực tế từ order
        Double amount = order.getTotalPrice();

        // 4. Gọi Gateway tương ứng
        PaymentGateway gateway;
        switch (paymentRequest.getPaymentMethod().toUpperCase()) {
            case "STRIPE":
                gateway = stripePaymentGateway;
                break;
            case "PAYPAL":
                gateway = paypalPaymentGateway;
                break;
            default:
                throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ!");
        }

        // 5. Gửi thanh toán (dùng DTO nội bộ)
        boolean success = gateway.processPayment(
                new InternalPaymentRequestDTO(order.getOrderId(), amount, paymentRequest.getPaymentMethod())
        );

        // 6. Lưu DB
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .amount(amount)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
        //7. Gửi event kết quả payment về order
        paymentProducer.sendPaymentResultEvent(PaymentResultEvent.builder()
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .success(success)
                .message(success ? "SUCCESS" : "FAILED")
                .timestamp(payment.getCreatedAt())
                .build());
        return payment;
    }

    @Override
    @Transactional
    public void processRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch cần hoàn tiền"));

        if (!PaymentStatus.SUCCESS.name().equals(payment.getStatus().name())) {
            throw new IllegalStateException("Chỉ có thể hoàn tiền cho các giao dịch thành công!");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        PaymentResultEvent event = PaymentResultEvent.builder()
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .success(true)
                .message("Hoàn tiền thành công!")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("payment-events", event);

        log.info("Hoàn tiền thành công cho Payment ID: " + paymentId);
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
