package sele906.dev.beluo_backend.chat.repository.conversation;

import sele906.dev.beluo_backend.chat.domain.Conversation;

import java.util.List;

public interface ConversationRepositoryCustom {
    List<Conversation> requestRecentConversations(String userId);
}
