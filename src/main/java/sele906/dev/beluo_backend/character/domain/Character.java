package sele906.dev.beluo_backend.character.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    private String characterFilePath;
    private String characterThumbFilePath;
    private String characterName;
    private String personality;
    private String firstMessage;
    private List<String> tag;
}
