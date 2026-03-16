package sele906.dev.beluo_backend.character.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blocked")
public class Blocked {

    @Id
    private ObjectId id;
    private String userId;
    private String characterId;
    private Instant createdAt;
}
