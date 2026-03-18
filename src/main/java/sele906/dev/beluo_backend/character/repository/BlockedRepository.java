package sele906.dev.beluo_backend.character.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.character.domain.Blocked;
import sele906.dev.beluo_backend.character.domain.Like;

import java.util.List;

public interface BlockedRepository extends MongoRepository<Blocked, String> {

    List<Blocked> findByUserId(String userId);

    void deleteByUserIdAndCharacterId(String userId, String characterId);

    List<Blocked> findByUserIdOrderByCreatedAtDesc(String userId);
}
