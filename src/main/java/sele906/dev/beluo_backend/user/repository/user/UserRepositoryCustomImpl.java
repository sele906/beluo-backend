package sele906.dev.beluo_backend.user.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.user.domain.User;

import java.time.Instant;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    public User userOverview(String userId) {
        Criteria criteria = Criteria.where("_id").is(userId);

        Query query = new Query(criteria);

        query.fields()
                .include("userImgUrl")
                .include("email")
                .include("credit")
                .include("name");

        return mongoTemplate.findOne(query, User.class);
    }

    public User userDetail(String userId) {
        Criteria criteria = Criteria.where("_id").is(userId);

        Query query = new Query(criteria);

        query.fields()
                .include("userImgUrl")
                .include("email")
                .include("name")
                .include("birth")
                .include("provider")
                .include("createdAt");

        return mongoTemplate.findOne(query, User.class);
    }

    @Override
    public void updateById(String userId, User user) {

        Query query = new Query(
                Criteria.where("_id").is(userId)
        );

        Update update = new Update()
                .set("name", user.getName())
                .set("birth", user.getBirth())
                .set("userImgUrl", user.getUserImgUrl());

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            update.set("email", user.getEmail());
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            update.set("password", user.getPassword());
        }

        mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public void updateAiModel(String userId, String aiModel) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().set("aiModel", aiModel);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public void incrementCredit(String userId, int amount) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().inc("credit", amount);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public void deductCredit(String userId, int amount) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().inc("credit", -amount);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public void anonymizeById(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update()
                .set("email", "deleted_" + userId)
                .set("name", "익명의 사용자")
                .unset("password")
                .unset("userImgUrl")
                .unset("refreshToken")
                .unset("providerId")
                .set("deletedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, User.class);
    }
}
