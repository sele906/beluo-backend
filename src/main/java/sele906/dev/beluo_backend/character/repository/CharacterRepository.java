package sele906.dev.beluo_backend.character.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.character.domain.Character;

public interface CharacterRepository extends MongoRepository<Character, String>, CharacterRepositoryCustom {
}
