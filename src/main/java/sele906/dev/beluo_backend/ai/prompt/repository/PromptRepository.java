package sele906.dev.beluo_backend.ai.prompt.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.chat.domain.Message;

public interface PromptRepository extends MongoRepository<Message, String>, PromptRepositoryCustom {
}
