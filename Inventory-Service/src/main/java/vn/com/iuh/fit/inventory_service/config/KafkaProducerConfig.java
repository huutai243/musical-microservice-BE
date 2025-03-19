package vn.com.iuh.fit.inventory_service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Producer để gửi InventoryValidationResultEvent
 * đến topic "inventory-validation-result"
 */
@Configuration
public class KafkaProducerConfig {

    private static final String BOOTSTRAP_SERVERS = "kafka:9092";

    @Bean
    public ProducerFactory<String, InventoryValidationResultEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Tắt type info headers nếu muốn tránh lỗi class name
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, InventoryValidationResultEvent> inventoryKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
