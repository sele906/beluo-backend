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
            throw new DataAccessException("크레딧 지급에 실패했어요. 잠시후 다시 시도해 주세요");
        }
    }

    //게스트 가입시 15 크레딧만
    public void grantGuestFreeBeta(String userId) {
        try {
            userRepository.incrementCredit(userId, 15);

            CreditHistory history = new CreditHistory();
            history.setUserId(userId);
            history.setType("GRANT");
            history.setSource("GUEST_FREE_BETA");
            history.setAmount(15);
            history.setExpiredAt(Instant.now().plus(1, ChronoUnit.DAYS));
            history.setMemo("베타 무료 크레딧");
            history.setCreatedAt(Instant.now());
            creditHistoryRepository.save(history);
        } catch (Exception e) {
            throw new DataAccessException("크레딧 지급에 실패했어요. 잠시후 다시 시도해 주세요");
        }
    }

    // 크레딧 사전 확인 (차감 없이 잔액만 검증)
    public void checkCredit(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다."));

        int cost = getCreditCost(user.getAiModel());

        if (user.getCredit() < cost) {
            throw new InvalidRequestException("크레딧이 부족해요. 충전 후 이용해 주세요");
        }
    }

    // 채팅/재생성 시 크레딧 차감 (모델별 비용 자동 적용)
    public void useCredit(String userId, String source) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다."));

        int cost = getCreditCost(user.getAiModel());

        if (user.getCredit() < cost) {
            throw new InvalidRequestException("크레딧이 부족해요. 충전 후 이용해 주세요");
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
        } catch (Exception e) {
            throw new DataAccessException("크레딧 차감에 실패했어요", e);
        }
    }

    // 만료된 GRANT 이력을 실제 유저 크레딧에 반영
    public void processExpiredCredits() {
        creditHistoryRepository.findExpiredGrants().forEach(history -> {
            User user = userRepository.findById(history.getUserId()).orElse(null);
            if (user == null) return;

            // 차감 후 0 미만 방지
            int deduct = Math.min(history.getAmount(), user.getCredit());
            userRepository.deductCredit(user.getId(), deduct);

            // EXPIRE 이력 기록
            CreditHistory expire = new CreditHistory();
            expire.setUserId(user.getId());
            expire.setType("EXPIRE");
            expire.setSource(history.getSource());
            expire.setAmount(-deduct);
            expire.setMemo("크레딧 만료");
            expire.setCreatedAt(Instant.now());
            creditHistoryRepository.save(expire);

            // 원본 GRANT 이력 만료 처리 완료 표시
            history.setExpired(true);
            creditHistoryRepository.save(history);
        });
    }

    // FREE_BETA 크레딧 보유 유저 credit 0으로 초기화 (베타 종료 시)
    public void expireFreeBetaCredits() {
        creditHistoryRepository.findActiveGrantsBySource("FREE_BETA").forEach(history -> {
            userRepository.setCreditById(history.getUserId(), 0);

            CreditHistory expire = new CreditHistory();
            expire.setUserId(history.getUserId());
            expire.setType("EXPIRE");
            expire.setSource("FREE_BETA");
            expire.setAmount(0);
            expire.setMemo("베타 종료로 인한 크레딧 초기화");
            expire.setCreatedAt(Instant.now());
            creditHistoryRepository.save(expire);

            history.setExpired(true);
            creditHistoryRepository.save(history);
        });
    }

    private int getCreditCost(String aiModel) {
        return switch (aiModel) {
            case "claude" -> 5;
            case "gpt"    -> 3;
            default       -> 1;
        };
    }
}
