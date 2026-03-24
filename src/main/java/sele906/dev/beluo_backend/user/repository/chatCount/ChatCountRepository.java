package sele906.dev.beluo_backend.user.repository.chatCount;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.user.domain.ChatCount;

public interface ChatCountRepository extends MongoRepository<ChatCount, String>, ChatCountRepositoryCustom {
}
