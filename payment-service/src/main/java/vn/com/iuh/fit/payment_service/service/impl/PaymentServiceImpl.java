package vn.com.iuh.fit.payment_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.client.OrderClient;
import vn.com.iuh.fit.payment_service.dto.InternalPaymentRequestDTO;
import vn.com.iuh.fit.payment_service.dto.OrderResponseDTO;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;
import vn.com.iuh.fit.payment_service.entity.OutboxEvent;
import vn.com.iuh.fit.payment_service.entity.Payment;
import vn.com.iuh.fit.payment_service.entity.ProcessedEvent;
import vn.com.iuh.fit.payment_service.enums.OrderStatus;
import vn.com.iuh.fit.payment_service.enums.PaymentStatus;
import vn.com.iuh.fit.payment_service.event.PaymentResultEvent;
import vn.com.iuh.fit.payment_service.gateway.PayPalPaymentGateway;
import vn.com.iuh.fit.payment_service.gateway.PaymentGateway;
import vn.com.iuh.fit.payment_service.gateway.StripePaymentGateway;
import vn.com.iuh.fit.payment_service.producer.PaymentProducer;
import vn.com.iuh.fit.payment_service.repository.OutboxEventRepository;
import vn.com.iuh.fit.payment_service.repository.PaymentRepository;
import vn.com.iuh.fit.payment_service.repository.ProcessedEventRepository;
import vn.com.iuh.fit.payment_service.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private StripePaymentGateway stripePaymentGateway;
    @Autowired private PayPalPaymentGateway paypalPaymentGateway;
    @Autowired private PaymentProducer paymentProducer;
    @Autowired private OrderClient orderClient;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private ObjectMapper objectMapper;



    @Override
    @Transactional
    public Payment processPayment(PaymentRequestDTO paymentRequest) {
        Long orderId = paymentRequest.getOrderId();
        String eventId = "payment-order-" + orderId;

        log.info("Xử lý thanh toán cho Order #" + orderId);

        //  Check đã xử lý chưa
        if (processedEventRepository.existsById(eventId)) {
            log.warn(" Đã xử lý thanh toán trước đó! eventId = {}", eventId);
            return paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán đã xử lý"));
        }

        //  Lưu eventId vào DB để đánh dấu đã xử lý
        processedEventRepository.save(new ProcessedEvent(eventId, LocalDateTime.now()));


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
        try {
            String payload = objectMapper.writeValueAsString(PaymentResultEvent.builder()
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .success(success)
                    .message(success ? "SUCCESS" : "FAILED")
                    .timestamp(payment.getCreatedAt())
                    .build());

            outboxEventRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Payment")
                    .aggregateId(String.valueOf(orderId))
                    .type("PaymentResultEvent")
                    .payload(payload)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build());

            log.info("Đã lưu PaymentResultEvent vào Outbox");

        } catch (JsonProcessingException e) {
            log.error("Lỗi serialize PaymentResultEvent", e);
            throw new RuntimeException("Không thể serialize PaymentResultEvent", e);
        }

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

    @Override
    @Transactional
    public void processRefundByOrderId(Long orderId, String reason) {
        log.info("Bắt đầu xử lý hoàn tiền theo yêu cầu từ Order-Service cho OrderId={}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch thanh toán với orderId = " + orderId));

        if (!PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            log.warn(" Chỉ hoàn tiền cho payment đã thành công! Status hiện tại = {}", payment.getStatus());
            return;
        }

        // Đánh dấu đã hoàn tiền
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        log.info(" Đã hoàn tiền thành công cho OrderId={}, lý do: {}", orderId, reason);
    }


}
