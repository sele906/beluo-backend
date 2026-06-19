package sele906.dev.beluo_backend.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fcmToken")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FcmToken {

    @Id
    private String id;
    @Indexed
    private String userId;
    private String token;
    private LocalDateTime createdAt;

    public FcmToken(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
