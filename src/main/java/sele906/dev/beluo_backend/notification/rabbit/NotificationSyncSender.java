package sele906.dev.beluo_backend.notification.rabbit;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.notification.service.FcmSendService;
import sele906.dev.beluo_backend.notification.service.FcmTokenService;

// 측정용 — 동기 알림 발송 (큐 안 거치고 직접 FCM 발송까지 대기)
@Profile("local")
@Service
public class NotificationSyncSender {

    private final FcmTokenService fcmTokenService;
    private final FcmSendService fcmSendService;

    public NotificationSyncSender(FcmTokenService fcmTokenService, FcmSendService fcmSendService) {
        this.fcmTokenService = fcmTokenService;
        this.fcmSendService = fcmSendService;
    }

    public void sendSync(NotificationMessage message) {

        var tokens = fcmTokenService.getTokensByUserId(message.getUserId());

        tokens.forEach(fcmToken -> {
            fcmSendService.sendToToken(
                    fcmToken.getToken(),
                    "Beluo 알림",
                    "AI가 답변을 완료했어요!",
                    message.getSessionId()
            );
        });
    }
}
