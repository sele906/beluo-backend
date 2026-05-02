package sele906.dev.beluo_backend.credit.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import sele906.dev.beluo_backend.credit.service.CreditService;
import sele906.dev.beluo_backend.user.repository.user.UserRepository;

@Component
public class CreditScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditService creditService;

    // 만료된 GRANT 크레딧을 유저 DB에 반영 (매일 자정 직전)
    @Scheduled(cron = "0 55 23 * * *")
    public void expireCredits() {
        creditService.processExpiredCredits();
    }

    // 매일 자정에 일반 유저 크레딧 50으로 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCredit() {
        userRepository.resetCreditForAllUsers(50);
    }

    // 베타 종료 - FREE_BETA 크레딧 보유 유저 credit 0으로 초기화 (2026년 5/1 자정 한정)
    @Scheduled(cron = "0 0 0 1 6 *")
    public void expireFreeBetaCredits() {
        if (LocalDate.now().getYear() != 2026) return;
        creditService.expireFreeBetaCredits();
    }
}
