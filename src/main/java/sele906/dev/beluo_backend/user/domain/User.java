package sele906.dev.beluo_backend.user.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user")
public class User {

    @Id
    private String id;
    private Instant createdAt;

    private String email;
    private String password;
    private String name;
    private String birth;
    private int credit;

    private String userImgUrl;
    private String aiModel = "gpt";

    private String role;
    private String refreshToken;
    private String provider;
    private String providerId;

    private Instant deletedAt;

    @Indexed(expireAfterSeconds = 0)
    private Instant guestExpiresAt;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
