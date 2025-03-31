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

    @Scheduled(fixedDelay = 3000)
    public void publishOutboxEvents() {
        log.info("[OutboxScheduler] Bắt đầu kiểm tra sự kiện PENDING...");
        List<OutboxEvent> events = outboxEventRepository.findByStatus("PENDING");

        log.info("[OutboxScheduler] Số lượng sự kiện PENDING: {}", events.size());

        for (OutboxEvent event : events) {
            log.info("[OutboxScheduler] Đang xử lý OutboxEvent ID={}, Type={}", event.getId(), event.getType());
            try {
                switch (event.getType()) {
                    case "InventoryValidationResultEvent" -> {
                        InventoryValidationResultEvent payload = objectMapper.readValue(
                                event.getPayload(), InventoryValidationResultEvent.class
                        );
                        kafkaTemplate.send("inventory-validation-result", payload);
                        log.info("[OutboxScheduler] Gửi thành công InventoryValidationResultEvent: orderId={}", payload.getOrderId());
                    }
                    default -> log.warn("[OutboxScheduler] Loại sự kiện không hỗ trợ: {}", event.getType());
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
