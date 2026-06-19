package sele906.dev.beluo_backend.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FirebaseInitializer {

    @Value("${FIREBASE_SERVICE_ACCOUNT:}")
    private String firebaseServiceAccount;

    @PostConstruct
    public void init() {
        try {

            InputStream serviceAccount;

            // 배포 환경
            if (firebaseServiceAccount != null && !firebaseServiceAccount.isBlank()) {
                serviceAccount = new ByteArrayInputStream(
                        firebaseServiceAccount.getBytes(StandardCharsets.UTF_8));

            // 로컬 환경
            } else {
                serviceAccount = new ClassPathResource(
                        "firebase/firebase-service-account.json").getInputStream();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (IOException e) {
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }
}
