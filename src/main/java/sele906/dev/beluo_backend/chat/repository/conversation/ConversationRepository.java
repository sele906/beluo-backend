package sele906.dev.beluo_backend.chat.repository.conversation;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.chat.domain.Conversation;

import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String>, ConversationRepositoryCustom {
    Optional<Conversation> findBySessionId(String sessionId);
}
