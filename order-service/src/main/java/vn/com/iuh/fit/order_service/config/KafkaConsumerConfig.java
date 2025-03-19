package vn.com.iuh.fit.order_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.event.InventoryValidationResultEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String GROUP_ID = "order-group";
    private Map<String, Object> commonConsumerConfig() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    return props;
}
    @Bean
    public ConsumerFactory<String, CheckoutEventDTO> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                commonConsumerConfig(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(CheckoutEventDTO.class))
        );
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CheckoutEventDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CheckoutEventDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
    @Bean
    public ConsumerFactory<String, InventoryValidationResultEvent> inventoryValidationResultConsumerFactory() {
        JsonDeserializer<InventoryValidationResultEvent> deserializer = new JsonDeserializer<>(InventoryValidationResultEvent.class);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                commonConsumerConfig(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryValidationResultEvent> inventoryValidationResultListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InventoryValidationResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inventoryValidationResultConsumerFactory());
        return factory;
    }
}
