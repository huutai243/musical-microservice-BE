package vn.com.iuh.fit.chat_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.chat_service.dto.ChatSessionResponse;
import vn.com.iuh.fit.chat_service.model.ChatSession;
import vn.com.iuh.fit.chat_service.repository.ChatSessionRepository;
import vn.com.iuh.fit.chat_service.service.SessionService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ChatSessionRepository chatSessionRepository;

    @Override
    public ChatSessionResponse createSession(String userId, String adminId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setAdminId(adminId);
        session.setCreatedAt(Instant.now());
        session.setStatus("OPEN");

        ChatSession saved = chatSessionRepository.save(session);

        return ChatSessionResponse.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .adminId(saved.getAdminId())
                .createdAt(saved.getCreatedAt())
                .status(saved.getStatus())
                .build();
    }

    @Override
    public ChatSessionResponse closeSession(String sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus("CLOSED");
        session.setClosedAt(Instant.now());
        ChatSession saved = chatSessionRepository.save(session);

        return ChatSessionResponse.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .adminId(saved.getAdminId())
                .createdAt(saved.getCreatedAt())
                .closedAt(saved.getClosedAt())
                .status(saved.getStatus())
                .build();
    }

    @Override
    public ChatSessionResponse getSession(String sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return ChatSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .adminId(session.getAdminId())
                .createdAt(session.getCreatedAt())
                .closedAt(session.getClosedAt())
                .status(session.getStatus())
                .build();
    }
}
