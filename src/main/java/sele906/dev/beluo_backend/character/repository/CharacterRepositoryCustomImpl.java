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
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CharacterRepositoryCustomImpl implements CharacterRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Character> requestRecentCharacters(List<String> blockedIds) {
        Criteria criteria = Criteria.where("isPublic").is(true);

        //차단된 캐릭터 제외
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
                .include("summary")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public List<Character> requestPopularCharacters(List<String> blockedIds) {

        Criteria criteria = Criteria.where("isPublic").is(true);

        //차단된 캐릭터 제외
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
                .include("summary")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public List<Character> requestLikedCharacters(List<String> characterIds) {

        if (characterIds == null || characterIds.isEmpty()) {
            return List.of();
        }

        List<ObjectId> objectIds = characterIds.stream().map(ObjectId::new).collect(Collectors.toList());
        Query query = new Query(Criteria.where("_id").in(objectIds));

        query.fields()
                .include("createdAt")
                .include("characterImgUrl")
                .include("characterName")
                .include("summary")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
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

    public List<Character> requestCreatedCharacters(String userId) {

        Criteria criteria = Criteria.where("userId").is(userId);

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(10);

        query.fields()
                .include("createdAt")
                .include("characterImgUrl")
                .include("characterName")
                .include("summary")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
    }

    public List<Character> createdCharacters(String userId) {

        Criteria criteria = Criteria.where("userId").is(userId);

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        //무한 스크롤??

        query.fields()
                .include("characterImgUrl")
                .include("characterName")
                .include("summary");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public List<Character> requestBlockedCharacters(List<String> characterIds) {

        if (characterIds == null || characterIds.isEmpty()) {
            return List.of();
        }

        List<ObjectId> objectIds = characterIds.stream().map(ObjectId::new).collect(Collectors.toList());
        Query query = new Query(Criteria.where("_id").in(objectIds));

        query.fields()
                .include("characterImgUrl")
                .include("characterName")
                .include("summary");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public List<Character> searchCharacters(String keyword, List<String> blockedIds) {

        Criteria criteria = Criteria.where("isPublic").is(true)
                .and("$text").is(Map.of("$search", keyword));

        if (blockedIds != null && !blockedIds.isEmpty()) {
            List<ObjectId> blockedObjectIds = blockedIds.stream().map(ObjectId::new).collect(Collectors.toList());
            criteria = criteria.and("_id").nin(blockedObjectIds);
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(20);

        query.fields()
                .include("createdAt")
                .include("characterImgUrl")
                .include("characterName")
                .include("summary")
                .include("tag");

        return mongoTemplate.find(query, Character.class);
    }

    @Override
    public void updateByIdAndUserId(String id, String userId, Character character) {

        Query query = new Query(
                Criteria.where("_id").is(id)
                        .and("userId").is(userId)
        );

        Update update = new Update()
                .set("characterName", character.getCharacterName())
                .set("characterImgUrl", character.getCharacterImgUrl())
                .set("summary", character.getSummary())
                .set("personality", character.getPersonality())
                .set("firstMessage", character.getFirstMessage())
                .set("tag", character.getTag())
                .set("isPublic", character.isPublic());

        mongoTemplate.updateFirst(query, update, Character.class);
    }
}
