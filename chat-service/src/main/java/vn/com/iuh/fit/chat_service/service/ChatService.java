package vn.com.iuh.fit.chat_service.service;

import vn.com.iuh.fit.chat_service.dto.ChatMessageRequest;
import vn.com.iuh.fit.chat_service.dto.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(String senderId, ChatMessageRequest request, String senderRole);
    List<ChatMessageResponse> getMessagesBySession(String sessionId);
}
