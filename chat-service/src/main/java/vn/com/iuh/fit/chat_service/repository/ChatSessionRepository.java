package vn.com.iuh.fit.chat_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.com.iuh.fit.chat_service.model.ChatSession;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    // Có thể thêm custom query
}
