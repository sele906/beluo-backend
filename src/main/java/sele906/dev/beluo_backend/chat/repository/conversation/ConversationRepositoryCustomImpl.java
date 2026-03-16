package sele906.dev.beluo_backend.chat.repository.conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;

import java.time.Instant;
import java.util.List;

@Repository
public class ConversationRepositoryCustomImpl implements ConversationRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    //채팅방 리스트 불러오기
    public List<Conversation> requestRecentConversations(String userId, List<String> blockedIds) {
        Criteria criteria = Criteria.where("userId").is(userId).and("lastChatAt").lt(Instant.now());
        if (blockedIds != null && !blockedIds.isEmpty()) {
            criteria = criteria.and("characterId").nin(blockedIds);
        }
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "lastChatAt"));
        query.limit(10);

        query.fields()
                .include("sessionId")
                .include("conversationName")
                .include("characterName")
                .include("characterImgUrl");

        List<Conversation> requestRecentConversations = mongoTemplate.find(query, Conversation.class);

        return requestRecentConversations;
    }

}
