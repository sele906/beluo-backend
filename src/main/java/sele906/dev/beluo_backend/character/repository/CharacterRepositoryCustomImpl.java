package sele906.dev.beluo_backend.character.repository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.character.domain.Character;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CharacterRepositoryCustomImpl implements CharacterRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Character> requestRecentCharacters(List<String> blockedIds) {
        Criteria criteria = Criteria.where("isPublic").is(true);
        if (blockedIds != null && !blockedIds.isEmpty()) {
            List<ObjectId> blockedObjectIds = blockedIds.stream().map(ObjectId::new).collect(Collectors.toList());
            criteria = criteria.and("_id").nin(blockedObjectIds);
        }
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(10);

        query.fields()
                .include("createdAt")
                .include("characterImgUrl")
                .include("characterName")
                .include("personality")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public List<Character> requestPopularCharacters(List<String> blockedIds) {
        Criteria criteria = Criteria.where("isPublic").is(true);
        if (blockedIds != null && !blockedIds.isEmpty()) {
            List<ObjectId> blockedObjectIds = blockedIds.stream().map(ObjectId::new).collect(Collectors.toList());
            criteria = criteria.and("_id").nin(blockedObjectIds);
        }
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "convCount"));
        query.limit(10);

        query.fields()
                .include("createdAt")
                .include("characterImgUrl")
                .include("characterName")
                .include("personality")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public List<Character> requestLikedCharacters(String userId) {
        return List.of(); //like 컬렉션 만들어야함
    }

    public void increaseConvCount(String characterId) {

        Query query = new Query(
                Criteria.where("_id").is(new ObjectId(characterId))
        );

        Update update = new Update().inc("convCount", 1);

        mongoTemplate.updateFirst(query, update, Character.class);
    }

    public void increaseLikeCount(String characterId) {

        Query query = new Query(
                Criteria.where("_id").is(new ObjectId(characterId))
        );

        Update update = new Update().inc("likeCount", 1);

        mongoTemplate.updateFirst(query, update, Character.class);
    }

    public void decreaseLikeCount(String characterId) {

        Query query = new Query(
                Criteria.where("_id").is(new ObjectId(characterId))
        );

        Update update = new Update().inc("likeCount", -1);

        mongoTemplate.updateFirst(query, update, Character.class);
    }
}
