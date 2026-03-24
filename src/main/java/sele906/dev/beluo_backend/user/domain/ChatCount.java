package sele906.dev.beluo_backend.user.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chatCount")
public class ChatCount {
    @Id
    private String id;
    private String userId;
    private Instant chatAt;
}
