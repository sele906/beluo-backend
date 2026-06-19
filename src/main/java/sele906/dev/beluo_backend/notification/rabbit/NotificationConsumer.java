package sele906.dev.beluo_backend.notification.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.config.rabbit.RabbitConfig;
import sele906.dev.beluo_backend.notification.service.FcmSendService;
import sele906.dev.beluo_backend.notification.service.FcmTokenService;

@Service
public class NotificationConsumer {

    private final FcmTokenService fcmTokenService;
    private final FcmSendService fcmSendService;

    public NotificationConsumer(FcmTokenService fcmTokenService, FcmSendService fcmSendService) {
        this.fcmTokenService = fcmTokenService;
        this.fcmSendService = fcmSendService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receive(NotificationMessage message) {

        // DB에서 testUser의 토큰 꺼내서 발송
        fcmTokenService.getTokensByUserId(message.getUserId()).forEach(fcmToken -> {
            fcmSendService.sendToToken(
                    fcmToken.getToken(),
                    "Beluo 알림",
                    "AI가 답변을 완료했어요!",
                    message.getSessionId()
            );
        });
    }
}
