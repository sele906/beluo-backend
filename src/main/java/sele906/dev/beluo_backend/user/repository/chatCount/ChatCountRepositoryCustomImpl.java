package sele906.dev.beluo_backend.user.repository.chatCount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Query;
import sele906.dev.beluo_backend.user.domain.ChatCount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Repository
public class ChatCountRepositoryCustomImpl implements ChatCountRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    public long countTodayChats(String userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Date startOfToday =
                Date.from(today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant());
        Date startOfTomorrow = Date.from(today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant());

        Criteria criteria = Criteria.where("userId").is(userId)
                .and("chatAt").gte(startOfToday).lt(startOfTomorrow);

        Query query = new Query(criteria);

        return mongoTemplate.count(query, ChatCount.class);
    }
}
