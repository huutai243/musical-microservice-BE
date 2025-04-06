package vn.com.iuh.fit.order_service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.order_service.entity.OutboxEvent;
import vn.com.iuh.fit.order_service.event.InventoryDeductionRequestEvent;
import vn.com.iuh.fit.order_service.event.NotificationOrderEvent;
import vn.com.iuh.fit.order_service.event.ValidateInventoryEvent;
import vn.com.iuh.fit.order_service.repository.OutboxEventRepository;

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
                    case "ValidateInventoryEvent" -> {
                        ValidateInventoryEvent payload = objectMapper.readValue(event.getPayload(), ValidateInventoryEvent.class);
                        kafkaTemplate.send("inventory-validation-events", payload);
                    }
                    case "InventoryDeductionEvent" -> {
                        InventoryDeductionRequestEvent payload = objectMapper.readValue(event.getPayload(), InventoryDeductionRequestEvent.class);
                        kafkaTemplate.send("inventory-deduction-events", payload);
                    }
                    case "NotificationEvent" -> {
                        NotificationOrderEvent payload = objectMapper.readValue(event.getPayload(), NotificationOrderEvent.class);
                        kafkaTemplate.send("notification-events", payload);
                    }
                    default -> log.warn("[OutboxScheduler] Loại sự kiện không xác định: {}", event.getType());
                }

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
            }
        }
    }
}
