package sele906.dev.beluo_backend.character.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.character.domain.Like;

import java.util.List;

public interface LikeRepository extends MongoRepository<Like, String> {
    boolean existsByUserIdAndCharacterId(String userId, String characterId);

    void deleteByUserIdAndCharacterId(String userId, String characterId);

    List<Like> findByUserId(String userId);
}
