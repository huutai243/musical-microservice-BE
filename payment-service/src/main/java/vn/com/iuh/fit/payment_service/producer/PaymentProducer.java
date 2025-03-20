package vn.com.iuh.fit.payment_service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.event.PaymentResultEvent;

import java.util.logging.Logger;

@Service
public class PaymentProducer {

    private static final Logger log = Logger.getLogger(PaymentProducer.class.getName());
    private static final String PAYMENT_RESULT_TOPIC = "payment-result-event";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentResultEvent(PaymentResultEvent event) {
        log.info(" Gửi PaymentResultEvent đến Kafka: " + event);
        kafkaTemplate.send(PAYMENT_RESULT_TOPIC, event.getOrderId().toString(), event);
    }
}
