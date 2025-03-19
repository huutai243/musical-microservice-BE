package vn.com.iuh.fit.inventory_service.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý các Kafka Topics.
 * Tự động tạo topic nếu chưa tồn tại.
 */
@Configuration
public class KafkaAdminTopicConfig {

    private static final String BOOTSTRAP_SERVERS = "kafka:9092";

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic inventoryValidationResultTopic() {
        return new NewTopic("inventory-validation-result", 3, (short) 1);
    }
}
