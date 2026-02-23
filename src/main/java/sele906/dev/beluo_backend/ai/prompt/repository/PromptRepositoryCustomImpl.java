package sele906.dev.beluo_backend.ai.prompt.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.chat.domain.Message;

import java.util.Collections;
import java.util.List;

@Repository
public class PromptRepositoryCustomImpl implements PromptRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    // 시스템 프롬프트 불러오기
    public Message systemMessage(String sessionId) {

        Query systemQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("system")
        );

        return mongoTemplate.findOne(systemQuery, Message.class);
    }

    //요약 프롬프트 불러오기
    public Message summaryMessage(String sessionId) {
        Query summaryQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        return mongoTemplate.findOne(summaryQuery, Message.class);
    }

    //최근 대화 프롬프트 불러오기
    public List<Message> recentMessage(String sessionId, int sinceLastSummaryCount) {
        Query chatQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").in("user", "assistant")
        );

        chatQuery.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        chatQuery.limit(sinceLastSummaryCount);

        List<Message> recentMessages = mongoTemplate.find(chatQuery, Message.class);
        Collections.reverse(recentMessages);

        return recentMessages;
    }
}
