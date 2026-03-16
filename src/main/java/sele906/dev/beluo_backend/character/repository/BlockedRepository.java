package sele906.dev.beluo_backend.character.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.character.domain.Blocked;

import java.util.List;

public interface BlockedRepository extends MongoRepository<Blocked, String> {
    List<Blocked> findByUserId(String userId);
    boolean existsByUserIdAndCharacterId(String userId, String characterId);
    void deleteByUserIdAndCharacterId(String userId, String characterId);
}
