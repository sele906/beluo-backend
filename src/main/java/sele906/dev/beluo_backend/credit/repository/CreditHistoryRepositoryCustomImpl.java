package sele906.dev.beluo_backend.credit.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.credit.domain.CreditHistory;

import java.util.List;

@Repository
public class CreditHistoryRepositoryCustomImpl implements CreditHistoryRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<CreditHistory> findHistoryByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, CreditHistory.class);
    }
}
