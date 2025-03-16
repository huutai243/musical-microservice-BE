package vn.com.iuh.fit.cart_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic checkoutTopic() {
        return new NewTopic("checkout-events", 3, (short) 1);
    }
}
