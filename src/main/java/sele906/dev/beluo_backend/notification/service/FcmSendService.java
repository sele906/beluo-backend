package sele906.dev.beluo_backend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.springframework.stereotype.Service;

@Service
public class FcmSendService {

    private final FcmTokenService fcmTokenService;

    public FcmSendService(FcmTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    public void sendToToken(String token, String title, String body, String sessionId) {

        // 메세지 = 알림 내용 + 받을 대상(토큰)
        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .putData("sessionId", sessionId)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ FCM 발송 성공: " + response);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();

            // 죽은 토큰 → 재시도 무의미, DB에서 삭제하고 조용히 넘어감
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                System.out.println("🗑️ 무효 토큰 삭제: " + token);
                fcmTokenService.deleteToken(token);
                return;   // 예외 안 던짐 → 재시도/DLQ로 안 감
            }

            // 그 외(일시적 장애 등) → 예외 던져서 재시도/DLQ
            System.out.println("❌ FCM 발송 실패(재시도 대상): " + e.getMessage());
            throw new RuntimeException("FCM 발송 실패", e);
        }
    }
}
