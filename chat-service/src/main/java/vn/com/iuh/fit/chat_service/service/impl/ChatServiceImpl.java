package vn.com.iuh.fit.chat_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.chat_service.dto.ChatMessageRequest;
import vn.com.iuh.fit.chat_service.dto.ChatMessageResponse;
import vn.com.iuh.fit.chat_service.model.ChatMessage;
import vn.com.iuh.fit.chat_service.repository.ChatMessageRepository;
import vn.com.iuh.fit.chat_service.service.ChatService;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessageResponse sendMessage(String senderId, ChatMessageRequest request, String senderRole) {
        // Tạo ChatMessage entity
        ChatMessage message = new ChatMessage();
        message.setSessionId(request.getSessionId());
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setTimestamp(Instant.now());
        message.setSenderRole(senderRole);

        // Lưu vào DB
        ChatMessage saved = chatMessageRepository.save(message);

        return ChatMessageResponse.builder()
                .id(saved.getId())
                .sessionId(saved.getSessionId())
                .senderId(saved.getSenderId())
                .receiverId(saved.getReceiverId())
                .content(saved.getContent())
                .type(saved.getType())
                .timestamp(saved.getTimestamp())
                .senderRole(saved.getSenderRole())
                .build();
    }

    @Override
    public List<ChatMessageResponse> getMessagesBySession(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId).stream()
                .map(msg -> ChatMessageResponse.builder()
                        .id(msg.getId())
                        .sessionId(msg.getSessionId())
                        .senderId(msg.getSenderId())
                        .receiverId(msg.getReceiverId())
                        .content(msg.getContent())
                        .type(msg.getType())
                        .timestamp(msg.getTimestamp())
                        .senderRole(msg.getSenderRole())
                        .build())
                .collect(Collectors.toList());
    }
}
