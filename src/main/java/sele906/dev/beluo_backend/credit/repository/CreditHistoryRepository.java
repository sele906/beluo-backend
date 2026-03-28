package sele906.dev.beluo_backend.credit.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.credit.domain.CreditHistory;

public interface CreditHistoryRepository extends MongoRepository<CreditHistory, String>, CreditHistoryRepositoryCustom {
}
