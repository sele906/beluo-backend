package sele906.dev.beluo_backend.character.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.chat.domain.Conversation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Repository
public class CharacterRepositoryCustomImpl implements CharacterRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Character> requestRecentCharacters() {
        Query query = new Query(
                Criteria.where("createdAt").lt(Instant.now())
        );
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(10);

        query.fields()
                .include("createdAt")
                .include("characterImgUrl")
                .include("characterName")
                .include("personality")
                .include("tag");

        List<Character> requestRecentCharacters = mongoTemplate.find(query, Character.class);

        return requestRecentCharacters;
    }
}
