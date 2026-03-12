package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.service.ConversationService;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    //캐릭터 상세정보 페이지와 이어짐
    @GetMapping("/create")
    public String createConversation(@RequestParam String characterId, Authentication auth) {

        if (auth == null) {
            throw new InvalidRequestException("로그인이 필요합니다");
        }

        String userId = auth.getName();

        return conversationService.createConversation(characterId, userId);
    }

    //최근 10개 채팅방 출력
    @GetMapping("/list")
    public List<Conversation> getConversationList(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return conversationService.conversationList(userId);
    }

    //채팅방 상세정보
    @GetMapping("/detail")
    public Map<String, Object> getConversationDetail(@RequestParam String sessionId) {
        return conversationService.getConversationDetail(sessionId);
    }
}
