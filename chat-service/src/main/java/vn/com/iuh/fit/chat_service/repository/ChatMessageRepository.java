package vn.com.iuh.fit.chat_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.com.iuh.fit.chat_service.model.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);
}
