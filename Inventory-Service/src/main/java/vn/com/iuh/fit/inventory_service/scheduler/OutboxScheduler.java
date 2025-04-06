package vn.com.iuh.fit.inventory_service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.inventory_service.entity.OutboxEvent;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.inventory_service.repository.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 500)
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatus("PENDING");
        for (OutboxEvent event : events) {
            try {
                switch (event.getType()) {
                    case "InventoryValidationResultEvent" -> {
                        InventoryValidationResultEvent payload = objectMapper.readValue(
                                event.getPayload(), InventoryValidationResultEvent.class
                        );
                        kafkaTemplate.send("inventory-validation-result", payload);
                    }
                    default -> log.warn("[OutboxScheduler] Loại sự kiện không hỗ trợ: {}", event.getType());
                }

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
            }
        }
    }
}
