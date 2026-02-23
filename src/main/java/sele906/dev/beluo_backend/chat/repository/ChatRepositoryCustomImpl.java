package sele906.dev.beluo_backend.chat.repository;


import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.chat.domain.Message;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Repository
public class ChatRepositoryCustomImpl implements ChatRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    //Chat

    //요약 후 대화 횟수 증가
    public UpdateResult afterSummaryChatCount(String chatRoomNum) {

        Query newSummaryQuery = new Query(
                Criteria.where("sessionId").is(chatRoomNum)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        Update summaryUpdate = new Update()
                .inc("sinceLastSummaryCount", 1);

        return mongoTemplate.updateFirst(newSummaryQuery, summaryUpdate, Message.class);
    }

    //최근 10개 대화 불러오기
    public List<Message> requestRecentChat(String chatRoomNum) {
        Query query = new Query(
                Criteria.where("sessionId").is(chatRoomNum)
                        .and("role").in("user", "assistant")
        );
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(10);

        List<Message> recentMessages = mongoTemplate.find(query, Message.class);
        Collections.reverse(recentMessages);

        return recentMessages;
    }

    //Summary

    //요약 데이터 가져오기
    public Message summaryMessage(String sessionId) {
        Query summaryQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        return mongoTemplate.findOne(summaryQuery, Message.class);
    }

    //요약할 최근 대화
    public List<Message> recentMessagesToSummarize(String sessionId, Instant lastSummarizedAt) {

        Query query = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").in("user", "assistant")
                        .and("createdAt").gt(lastSummarizedAt)
        );

        query.with(Sort.by(Sort.Direction.ASC, "createdAt"));
        query.limit(10);

        return mongoTemplate.find(query, Message.class);
    }

    //요약할 데이터 업데이트
    public UpdateResult summaryDataUpdate(String sessionId, String finishedSummary, Instant newLastSummarizedAt) {
        Query newSummaryQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        Update summaryUpdate = new Update()
                .set("content", finishedSummary)
                .set("lastSummarizedAt", newLastSummarizedAt)
                .set("sinceLastSummaryCount", 1)
                .set("isSummarizing", false)
                .inc("summaryVersion", 1);

        return mongoTemplate.updateFirst(newSummaryQuery, summaryUpdate, Message.class);
    }

    //ChatRoom
}
