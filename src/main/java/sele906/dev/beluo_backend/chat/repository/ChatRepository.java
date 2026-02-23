package sele906.dev.beluo_backend.chat.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;

import java.util.Optional;

public interface ChatRepository extends MongoRepository<Message, String>, ChatRepositoryCustom {

    // sessionId로 현재 대화 조회
    //Optional<Message> findBySessionId(String sessionId);


}
