package vn.com.iuh.fit.chat_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import vn.com.iuh.fit.chat_service.dto.ChatMessageRequest;
import vn.com.iuh.fit.chat_service.dto.ChatMessageResponse;
import vn.com.iuh.fit.chat_service.service.ChatService;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;

    // Khi client gửi message đến /app/sendMessage
    @MessageMapping("/sendMessage")
    @SendToUser("/queue/reply") // Hoặc bạn dùng /topic/ tuỳ kịch bản
    public ChatMessageResponse handleSendMessage(
            @Payload ChatMessageRequest request,
            Authentication authentication
    ) {
        // Lấy userId từ authentication (username), hoặc cắt ra
        String senderRole = "USER"; // Hoặc check role admin
        String senderId = authentication.getName(); // subject = username

        // Gọi service để lưu DB
        ChatMessageResponse response = chatService.sendMessage(senderId, request, senderRole);

        // Trả trực tiếp cho user (demo). Thường bạn broadcast /topic/room/{sessionId}
        return response;
    }
}
