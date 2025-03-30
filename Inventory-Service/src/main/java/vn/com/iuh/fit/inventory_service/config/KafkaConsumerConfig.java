package vn.com.iuh.fit.inventory_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import vn.com.iuh.fit.inventory_service.event.InventoryDeductionRequestEvent;
import vn.com.iuh.fit.inventory_service.event.ValidateInventoryEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Kafka Consumer:
 * Lắng nghe các sự kiện và parse JSON thành các Event cụ thể.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String GROUP_ID = "inventory-group";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaConsumerConfig(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> eventClass) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(eventClass);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerFactory(Class<T> eventClass) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(eventClass));
        factory.setCommonErrorHandler(defaultErrorHandler());
        return factory;
    }

    @Bean(name = "validateInventoryEventListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ValidateInventoryEvent> validateInventoryEventListenerFactory() {
        return createListenerFactory(ValidateInventoryEvent.class);
    }

    @Bean(name = "inventoryDeductionListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, InventoryDeductionRequestEvent> inventoryDeductionListenerFactory() {
        return createListenerFactory(InventoryDeductionRequestEvent.class);
    }
    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(3000L, 3)); // retry 3 lần, delay 3s
    }

}
