package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.ChatService;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.user.service.ChatCountService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatCountService chatCountService;

    //메세지 보내기
    @PostMapping("/send")
    public Map<String, String> chatSend(@RequestBody Map<String, String> body, Authentication auth) {

        //유저 챗 카운트
        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        if (chatCountService.chatLimit(userId)) {
            throw new InvalidRequestException("최대 채팅 횟수가 50회를 초과하였습니다");
        }
        chatCountService.chatCountSave(userId);

        //정보 가져오기
        String userMessage = body.get("message");
        String sessionId = body.get("sessionId");

        //유저 메세지 db에 저장
        //role
        String userRole = "user";

        //content
        String userContent = userMessage;

        //유저 메세지 db 저장
        Message savedUserMessage = chatService.chatDataSave(userRole, userContent, sessionId);
        chatService.afterSummaryChatCount(sessionId);

        //프롬프트에 최근 대화 합쳐서 api 보내기 (ai 답변은 저장 X, confirm에서 저장)
        String reply = chatService.sendChatApi(sessionId, userId);

        return Map.of("reply", reply, "userMessageId", savedUserMessage.getId());
    }

    //다시 생성 (db 저장 없이 ai 답변만 생성, db 마지막이 유저 메세지라 컨텍스트 동일)
    @PostMapping("/regenerate")
    public Map<String, String> chatRegenerate(@RequestBody Map<String, String> body, Authentication auth) {

        //유저 챗 카운트
        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        if (chatCountService.chatLimit(userId)) {
            throw new InvalidRequestException("최대 채팅 횟수가 50회를 초과하였습니다");
        }
        chatCountService.chatCountSave(userId);

        //답변 재생성
        String sessionId = body.get("sessionId");
        String reply = chatService.sendChatApi(sessionId, userId);

        return Map.of("reply", reply);
    }

    //선택된 답변 DB 저장
    @PostMapping("/confirm")
    public Map<String, String> chatConfirm(@RequestBody Map<String, String> body) {

        String sessionId = body.get("sessionId");
        String aiRole = "assistant";
        String aiContent = body.get("reply");

        //ai 메세지 db 저장
        Message savedAiMessage = chatService.chatDataSave(aiRole, aiContent, sessionId);
        chatService.afterSummaryChatCount(sessionId);

        return Map.of("messageId", savedAiMessage.getId());
    }

    //메세지 출력
    @GetMapping("/messages")
    public Map<String, Object> getMessageList(
            @RequestParam String sessionId,
            @RequestParam(required = false) String before
    ) {
        return chatService.requestRecentChat(sessionId, before);
    }

    //메세지 수정
    @PatchMapping("/edit")
    public void chatEdit(@RequestBody Map<String, String> body) {
        String messageId = body.get("chatId");
        String sessionId = body.get("sessionId");
        String content = body.get("content");

        chatService.chatEdit(sessionId, messageId, content);
    }

}
