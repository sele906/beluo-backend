package sele906.dev.beluo_backend.credit.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.credit.domain.CreditHistory;

import java.time.Instant;
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

    // expiredAt이 지났고 아직 만료 처리 안 된 GRANT 이력 조회
    @Override
    public List<CreditHistory> findExpiredGrants() {
        Query query = new Query(
                Criteria.where("type").is("GRANT")
                        .and("expiredAt").lt(Instant.now())
                        .and("expired").is(false)
        );
        return mongoTemplate.find(query, CreditHistory.class);
    }

    // 특정 source의 아직 만료 처리 안 된 GRANT 이력 조회
    @Override
    public List<CreditHistory> findActiveGrantsBySource(String source) {
        Query query = new Query(
                Criteria.where("type").is("GRANT")
                        .and("source").is(source)
                        .and("expired").is(false)
        );
        return mongoTemplate.find(query, CreditHistory.class);
    }
}
