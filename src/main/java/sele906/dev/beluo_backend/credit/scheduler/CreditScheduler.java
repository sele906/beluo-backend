package sele906.dev.beluo_backend.credit.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import sele906.dev.beluo_backend.credit.service.CreditService;

@Component
public class CreditScheduler {

    @Autowired
    private CreditService creditService;

    // 만료된 GRANT 크레딧을 유저 DB에 반영 (매일 자정 직전)
    @Scheduled(cron = "0 55 23 * * *")
    public void expireCredits() {
        creditService.processExpiredCredits();
    }


}
