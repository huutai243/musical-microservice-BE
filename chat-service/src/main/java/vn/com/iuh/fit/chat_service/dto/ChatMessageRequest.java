
package vn.com.iuh.fit.chat_service.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String sessionId;
    private String receiverId;
    private String content;
    private String type;
}