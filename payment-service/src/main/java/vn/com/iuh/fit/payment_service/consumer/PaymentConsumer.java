package vn.com.iuh.fit.payment_service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.event.RefundRequestEvent;
import vn.com.iuh.fit.payment_service.service.PaymentService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "refund-request-events",
            groupId = "payment-group",
            containerFactory = "refundRequestListenerFactory"
    )
    public void handleRefundRequestEvent(RefundRequestEvent event) {
        try {
            log.info(" Nhận RefundRequestEvent từ Kafka: {}", event);
            paymentService.processRefundByOrderId(event.getOrderId(), event.getReason());
        } catch (Exception e) {
            log.error(" Lỗi xử lý RefundRequestEvent: {}", e.getMessage(), e);
        }
    }
}
