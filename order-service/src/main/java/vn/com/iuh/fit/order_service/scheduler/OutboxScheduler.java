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

    @Scheduled(fixedDelay = 3000)
    public void publishOutboxEvents() {
        log.info("[OutboxScheduler] Bắt đầu kiểm tra sự kiện PENDING...");
        List<OutboxEvent> events = outboxEventRepository.findByStatus("PENDING");
        log.info("[OutboxScheduler] Số lượng sự kiện PENDING: {}", events.size());

        for (OutboxEvent event : events) {
            try {
                log.info("[OutboxScheduler] Đang xử lý OutboxEvent ID={}, Type={}", event.getId(), event.getType());

                switch (event.getType()) {
                    case "ValidateInventoryEvent" -> {
                        log.info("[OutboxScheduler] Gửi ValidateInventoryEvent tới Kafka topic: inventory-validation-events");
                        ValidateInventoryEvent payload = objectMapper.readValue(event.getPayload(), ValidateInventoryEvent.class);
                        kafkaTemplate.send("inventory-validation-events", payload);
                        log.info("[OutboxScheduler] Gửi thành công ValidateInventoryEvent: orderId={}", payload.getOrderId());
                    }
                    case "InventoryDeductionEvent" -> {
                        log.info("[OutboxScheduler] Gửi InventoryDeductionEvent tới Kafka topic: inventory-deduction-events");
                        InventoryDeductionRequestEvent payload = objectMapper.readValue(event.getPayload(), InventoryDeductionRequestEvent.class);
                        kafkaTemplate.send("inventory-deduction-events", payload);
                        log.info("[OutboxScheduler] Gửi thành công InventoryDeductionEvent: orderId={}", payload.getOrderId());
                    }
                    case "NotificationEvent" -> {
                        log.info("[OutboxScheduler] Gửi NotificationEvent tới Kafka topic: notification-events");
                        NotificationOrderEvent payload = objectMapper.readValue(event.getPayload(), NotificationOrderEvent.class);
                        kafkaTemplate.send("notification-events", payload);
                        log.info("[OutboxScheduler] Gửi thành công NotificationEvent: orderId={}", payload.getOrderId());
                    }
                    default -> log.warn("[OutboxScheduler] Loại sự kiện không xác định: {}", event.getType());
                }

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("[OutboxScheduler] Đã cập nhật trạng thái OutboxEvent ID={} thành SENT", event.getId());

            } catch (Exception e) {
                log.error("[OutboxScheduler] Lỗi gửi OutboxEvent ID={}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
}
