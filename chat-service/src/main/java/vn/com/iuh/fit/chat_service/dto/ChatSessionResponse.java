package vn.com.iuh.fit.chat_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatSessionResponse {
    private String id;
    private String userId;
    private String adminId;
    private Instant createdAt;
    private Instant closedAt;
    private String status;
}
