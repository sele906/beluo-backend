package sele906.dev.beluo_backend.character.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "character")
public class Character {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private Instant createdAt;

    private String characterImgUrl;
    private String characterName;

    private String summary;
    private String personality;
    private String firstMessage;
    private List<String> tag;

    private String userId;

    private boolean isPublic;
    private int convCount;
    private int likeCount;
}
