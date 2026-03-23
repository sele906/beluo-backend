package sele906.dev.beluo_backend.character.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    private PersonalityJson personalityJson;
    private String firstMessage;
    private List<String> tag;

    private String userId;

    @JsonProperty("isPublic")
    private boolean isPublic;
    private int convCount;
    private int likeCount;
    private Instant deletedAt;
}
