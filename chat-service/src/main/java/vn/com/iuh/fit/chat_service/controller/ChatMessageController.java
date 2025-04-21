package vn.com.iuh.fit.chat_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.chat_service.dto.ChatMessageRequest;
import vn.com.iuh.fit.chat_service.dto.ChatMessageResponse;
import vn.com.iuh.fit.chat_service.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatService chatService;

    // Lấy lịch sử chat theo session
    @GetMapping("/{sessionId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(chatService.getMessagesBySession(sessionId));
    }

    // Gửi tin nhắn qua REST (nếu bạn muốn, thay vì WebSocket)
    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestBody ChatMessageRequest request,
            Authentication authentication
    ) {
        String senderId = authentication.getName(); // subject
        String senderRole = "USER"; // tuỳ logic check
        ChatMessageResponse resp = chatService.sendMessage(senderId, request, senderRole);
        return ResponseEntity.ok(resp);
    }
}
