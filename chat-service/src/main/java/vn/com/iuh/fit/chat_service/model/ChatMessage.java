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
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;

    private String sessionId;   // ID của phiên chat (nếu có)
    private String senderId;
    private String receiverId;

    private String content;
    private String type;        // TEXT, IMAGE, FILE...
    private Instant timestamp;
    private String senderRole;  // USER, ADMIN, BOT
}
