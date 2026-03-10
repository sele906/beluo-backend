package sele906.dev.beluo_backend.auth.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
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
    private String email;
    private String password;
    private String name;

    //private String profileImgUrl;

    private String provider;
    private String providerId;
    private String role;
    private String refreshToken;
    private Instant createdAt;
}
