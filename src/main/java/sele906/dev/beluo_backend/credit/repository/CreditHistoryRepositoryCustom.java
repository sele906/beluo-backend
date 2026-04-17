package sele906.dev.beluo_backend.credit.repository;

import sele906.dev.beluo_backend.credit.domain.CreditHistory;

import java.util.List;

public interface CreditHistoryRepositoryCustom {
    List<CreditHistory> findHistoryByUserId(String userId);
    List<CreditHistory> findExpiredGrants();
    List<CreditHistory> findActiveGrantsBySource(String source);
}
