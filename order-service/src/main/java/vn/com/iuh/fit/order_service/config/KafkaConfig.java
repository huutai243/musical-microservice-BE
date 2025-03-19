package vn.com.iuh.fit.order_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.kafka.support.serializer.JsonSerializer;
import vn.com.iuh.fit.order_service.event.ValidateInventoryEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    //  Tạo các Kafka Topics
    @Bean
    public NewTopic orderEventsTopic() {
        return new NewTopic("order-events", 3, (short) 1);
    }

    @Bean
    public NewTopic orderConfirmedTopic() {
        return new NewTopic("order-confirmed-events", 3, (short) 1);
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return new NewTopic("order-cancelled-events", 3, (short) 1);
    }

    @Bean
    public NewTopic orderShippedTopic() {
        return new NewTopic("order-shipped-events", 3, (short) 1);
    }

    @Bean
    public NewTopic orderDeliveredTopic() {
        return new NewTopic("order-delivered-events", 3, (short) 1);
    }
    @Bean
    public NewTopic inventoryValidationTopic() {
        return new NewTopic("inventory-validation-events", 3, (short) 1);
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return new NewTopic("notification-events", 3, (short) 1);
    }
    @Bean
    public ProducerFactory<String, ValidateInventoryEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false); //  Tắt Transaction

        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public KafkaTemplate<String, ValidateInventoryEvent> validateInventoryKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
