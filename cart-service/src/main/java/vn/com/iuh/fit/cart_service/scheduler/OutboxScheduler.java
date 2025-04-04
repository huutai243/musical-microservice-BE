package vn.com.iuh.fit.cart_service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.cart_service.entity.OutboxEvent;
import vn.com.iuh.fit.cart_service.event.CheckoutEvent;
import vn.com.iuh.fit.cart_service.repository.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3000)
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatus("PENDING");
        for (OutboxEvent event : events) {
            try {
                if ("CheckoutEvent".equals(event.getType())) {
                    CheckoutEvent payload = objectMapper.readValue(event.getPayload(), CheckoutEvent.class);
                    kafkaTemplate.send("checkout-events", payload);
                }
                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
            }
        }
    }
}
