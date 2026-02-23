package sele906.dev.beluo_backend.chat.repository.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.chat.domain.Message;

public interface MessageRepository extends MongoRepository<Message, String>, MessageRepositoryCustom {

    // sessionId로 현재 대화 조회
    //Optional<Message> findBySessionId(String sessionId);


}
