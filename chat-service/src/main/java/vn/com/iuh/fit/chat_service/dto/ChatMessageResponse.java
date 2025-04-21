package vn.com.iuh.fit.chat_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatMessageResponse {
    private String id;
    private String sessionId;
    private String senderId;
    private String receiverId;
    private String content;
    private String type;
    private Instant timestamp;
    private String senderRole;
}