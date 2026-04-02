package sele906.dev.beluo_backend.chat.repository.conversation;

import sele906.dev.beluo_backend.chat.domain.Conversation;

import java.time.Instant;
import java.util.List;

public interface ConversationRepositoryCustom {
    List<Conversation> findRecentConversations(String userId, List<String> blockedIds, Instant before);

    void anonymizeByUserId(String userId);

    void anonymizeOneByUserId(String sessionId, String userId);

    void updateConversationName(String sessionId, String conversationName);
}
