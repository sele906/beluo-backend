package sele906.dev.beluo_backend.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message")
public class Message {
    @Id
    private ObjectId id;
    private String sessionId;
    private String role; // system | user | assistant
    private String content;
    private Instant createdAt;
    private String type; // system | user | assistant

    //요약
    //나중에 분리 필요?
    private Instant lastSummarizedAt;
    private Integer sinceLastSummaryCount;
    private Integer summaryVersion;
    private Boolean isSummarizing;

}
