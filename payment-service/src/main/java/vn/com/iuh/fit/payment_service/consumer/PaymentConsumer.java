package vn.com.iuh.fit.payment_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.event.PaymentConfirmedEvent;
import vn.com.iuh.fit.payment_service.event.PaymentFailedEvent;

@Service
public class PaymentConsumer {

    @KafkaListener(topics = "payment-confirmed-events", groupId = "payment-group")
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        System.out.println(" Nhận sự kiện thanh toán thành công: " + event);
    }

    @KafkaListener(topics = "payment-failed-events", groupId = "payment-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        System.out.println(" Nhận sự kiện thanh toán thất bại: " + event);
    }
}
