package vn.com.iuh.fit.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentEventsTopic() {
        return new NewTopic("payment-events", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentConfirmedTopic() {
        return new NewTopic("payment-confirmed-events", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed-events", 3, (short) 1);
    }
}
