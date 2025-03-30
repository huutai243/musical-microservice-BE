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
        log.info("[OutboxScheduler] Bắt đầu kiểm tra sự kiện PENDING...");
        List<OutboxEvent> events = outboxEventRepository.findByStatus("PENDING");

        log.info("[OutboxScheduler] Số lượng sự kiện PENDING: {}", events.size());

        for (OutboxEvent event : events) {
            try {
                log.info("[OutboxScheduler] Đang xử lý OutboxEvent ID={}, Type={}", event.getId(), event.getType());

                if ("CheckoutEvent".equals(event.getType())) {
                    CheckoutEvent payload = objectMapper.readValue(event.getPayload(), CheckoutEvent.class);
                    log.info("[OutboxScheduler] Đang gửi sự kiện CheckoutEvent đến Kafka topic: checkout-events");
                    kafkaTemplate.send("checkout-events", payload);
                    log.info("[OutboxScheduler] Gửi thành công sự kiện CheckoutEvent: {}", payload.getEventId());
                }

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("[OutboxScheduler] Cập nhật trạng thái sự kiện ID={} thành SENT", event.getId());

            } catch (Exception e) {
                log.error("[OutboxScheduler] Lỗi khi xử lý sự kiện ID={}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
}
