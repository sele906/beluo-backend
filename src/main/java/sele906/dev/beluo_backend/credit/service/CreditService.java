package sele906.dev.beluo_backend.credit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.credit.domain.CreditHistory;
import sele906.dev.beluo_backend.credit.repository.CreditHistoryRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.user.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CreditService {

    @Autowired
    private CreditHistoryRepository creditHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    // 가입 시 베타 무료 크레딧 50개 지급
    public void grantFreeBeta(String userId) {
        try {
            userRepository.incrementCredit(userId, 50);

            CreditHistory history = new CreditHistory();
            history.setUserId(userId);
            history.setType("GRANT");
            history.setSource("FREE_BETA");
            history.setAmount(50);
            history.setExpiredAt(Instant.now().plus(60, ChronoUnit.DAYS));
            history.setMemo("베타 무료 크레딧");
            history.setCreatedAt(Instant.now());
            creditHistoryRepository.save(history);
        } catch (Exception e) {
            throw new DataAccessException("크레딧 지급에 실패했습니다.");
        }
    }

    // 채팅/재생성 시 크레딧 차감 (모델별 비용 자동 적용)
    public void useCredit(String userId, String source) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다."));

        int cost = getCreditCost(user.getAiModel());

        if (user.getCredit() < cost) {
            throw new InvalidRequestException("크레딧이 부족합니다");
        }

        try {
            userRepository.deductCredit(userId, cost);

            CreditHistory history = new CreditHistory();
            history.setUserId(userId);
            history.setType("USE");
            history.setSource(source);
            history.setAmount(-cost);
            history.setCreatedAt(Instant.now());
            creditHistoryRepository.save(history);
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("크레딧 차감에 실패했습니다.");
        }
    }

    private int getCreditCost(String aiModel) {
        return switch (aiModel) {
            case "claude" -> 5;
            case "gpt"    -> 2;
            default       -> 1; // free
        };
    }
}
