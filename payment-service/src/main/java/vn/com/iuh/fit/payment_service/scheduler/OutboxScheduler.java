package vn.com.iuh.fit.payment_service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.payment_service.entity.OutboxEvent;
import vn.com.iuh.fit.payment_service.event.PaymentResultEvent;
import vn.com.iuh.fit.payment_service.repository.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3000)
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatus("PENDING");
        for (OutboxEvent event : events) {
            try {
                if ("PaymentResultEvent".equals(event.getType())) {
                    PaymentResultEvent payload = objectMapper.readValue(event.getPayload(), PaymentResultEvent.class);
                    kafkaTemplate.send("payment-result-event", payload);
                }
                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Lỗi gửi OutboxEvent: {}", event.getId(), e);
            }
        }
    }
}
