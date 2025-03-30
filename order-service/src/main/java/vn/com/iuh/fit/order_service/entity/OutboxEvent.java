package vn.com.iuh.fit.order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    private UUID id;
    private String aggregateType;
    private String aggregateId;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private String status;        // PENDING, SENT
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}