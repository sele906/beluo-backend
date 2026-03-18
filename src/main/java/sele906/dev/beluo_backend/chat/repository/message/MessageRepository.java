package sele906.dev.beluo_backend.chat.repository.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.chat.domain.Message;

public interface MessageRepository extends MongoRepository<Message, String>, MessageRepositoryCustom {
}
