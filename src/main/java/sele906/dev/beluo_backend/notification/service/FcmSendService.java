package sele906.dev.beluo_backend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class FcmSendService {

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
            System.out.println("❌ FCM 발송 실패: " + e.getMessage());
            throw new RuntimeException("FCM 발송 실패", e);   // 예외를 다시 던져야 재시도/DLQ 작동
        }
    }
}
