package sele906.dev.beluo_backend.character.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.character.domain.Character;

import java.util.List;

public interface CharacterRepository extends MongoRepository<Character, String>, CharacterRepositoryCustom {
    void deleteByIdAndUserId(String characterId, String userId);
}
