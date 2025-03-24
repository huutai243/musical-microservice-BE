package vn.com.iuh.fit.iuh.notification_service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.iuh.notification_service.event.NotificationOrderEvent;
import vn.com.iuh.fit.iuh.notification_service.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(NotificationOrderEvent event) {
        log.info(" Nhận được NotificationOrderEvent từ Kafka: {}", event);
        notificationService.handleNotification(event);
    }
}
