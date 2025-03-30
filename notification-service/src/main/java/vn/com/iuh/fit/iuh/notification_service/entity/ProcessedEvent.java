package vn.com.iuh.fit.iuh.notification_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "processed_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @Id
    private String eventId;
    private LocalDateTime processedAt;
}
