package vn.com.iuh.fit.chat_service.service;

import vn.com.iuh.fit.chat_service.dto.ChatSessionResponse;

public interface SessionService {
    ChatSessionResponse createSession(String userId, String adminId);
    ChatSessionResponse closeSession(String sessionId);
    ChatSessionResponse getSession(String sessionId);
}
