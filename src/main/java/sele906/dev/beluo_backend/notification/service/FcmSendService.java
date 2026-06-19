package sele906.dev.beluo_backend.notification.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

@Service
public class FcmSendService {

    private final FcmTokenService fcmTokenService;

    public FcmSendService(FcmTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    public void sendToToken(String token, String title, String body, String sessionId) {

        // webpush 알림 — OS가 직접 띄움 (모바일에서 안정적)
        WebpushNotification webpushNotification = WebpushNotification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        WebpushConfig webpushConfig = WebpushConfig.builder()
                .setNotification(webpushNotification)
                .putData("sessionId", sessionId)   // 클릭 이동용
                .build();

        // 메세지 = 알림 내용 + 받을 대상(토큰)
        Message message = Message.builder()
                .setToken(token)
                .setWebpushConfig(webpushConfig)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ FCM 발송 성공: " + response);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();

            // 죽은 토큰
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                System.out.println("🗑️ 무효 토큰 삭제: " + token);
                fcmTokenService.deleteToken(token);
                return;   // 예외 안 던짐
            }

            // 그 외 예외 던져서 재시도/DLQ
            System.out.println("❌ FCM 발송 실패(재시도 대상): " + e.getMessage());
            throw new RuntimeException("FCM 발송 실패", e);
        }
    }
}
