package sele906.dev.beluo_backend.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import sele906.dev.beluo_backend.chat.domain.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversation")
public class Conversation {
    @Id
    private ObjectId id;
    private String sessionId;
    private Instant createdAt;
    private Instant lastChatAt;

    private String conversationName;

    private String characterId;
    private String characterName;
    private String characterImgUrl;

    private String userId;
    private String userEmail;
    private String userName;
    private String userImgUrl;

}