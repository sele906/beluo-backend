package sele906.dev.beluo_backend.user.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.user.domain.User;

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
                .set("userImgUrl", user.getUserImgUrl());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            update.set("password", user.getPassword());
        }

        mongoTemplate.updateFirst(query, update, User.class);
    }
}
