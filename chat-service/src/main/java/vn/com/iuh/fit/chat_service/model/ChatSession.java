package vn.com.iuh.fit.chat_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_sessions")
public class ChatSession {
    @Id
    private String id;

    private String userId;
    private String adminId;       // admin handling session
    private Instant createdAt;
    private Instant closedAt;
    private String status;        // OPEN, CLOSED...
}
