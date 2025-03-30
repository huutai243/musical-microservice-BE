package vn.com.iuh.fit.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @Id
    private String eventId;
    private String correlationId;
    private LocalDateTime processedAt;
}