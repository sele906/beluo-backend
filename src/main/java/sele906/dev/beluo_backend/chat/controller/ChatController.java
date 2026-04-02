package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.ChatService;
import sele906.dev.beluo_backend.credit.service.CreditService;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CreditService creditService;

    //메세지 보내기
    @PostMapping("/send")
    public Map<String, String> chatSend(@RequestBody Map<String, String> body, Authentication auth) {

        String userId = null;
        if (auth != null) {
            userId = auth.getName();
        }

        creditService.checkCredit(userId);

        String userMessage = body.get("message");
        String sessionId = body.get("sessionId");

        // 유저 메세지 db 저장
        Message savedUserMessage = chatService.chatDataSave("user", userMessage, sessionId);
        chatService.afterSummaryChatCount(sessionId);

        // AI 호출 실패 시 유저 메세지 롤백, 크레딧 차감 안 함
        String reply;
        try {
            reply = chatService.sendChatApi(sessionId, userId);
        } catch (Exception e) {
            chatService.deleteMessage(savedUserMessage.getId());
            throw e;
        }

        // 성공 시에만 크레딧 차감
        creditService.useCredit(userId, "CHAT");

        return Map.of("reply", reply, "userMessageId", savedUserMessage.getId());
    }

    //다시 생성 (db 저장 없이 ai 답변만 생성, db 마지막이 유저 메세지라 컨텍스트 동일)
    @PostMapping("/regenerate")
    public Map<String, String> chatRegenerate(@RequestBody Map<String, String> body, Authentication auth) {

        String userId = null;
        if (auth != null) {
            userId = auth.getName();
        }

        creditService.checkCredit(userId);

        // AI 호출 성공 시에만 크레딧 차감
        String sessionId = body.get("sessionId");
        String reply = chatService.sendChatApi(sessionId, userId);

        creditService.useCredit(userId, "REGENERATE");

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
    @GetMapping("/messages/{sessionId}")
    public Map<String, Object> getMessageList(
            @PathVariable String sessionId,
            @RequestParam(required = false) String before
    ) {
        return chatService.requestRecentChat(sessionId, before);
    }

    //고아 메세지 롤백 (유저 이탈 시 프론트에서 호출, sendBeacon은 POST만 지원)
    @PostMapping("/orphan")
    public void deleteOrphanMessage(@RequestParam String sessionId) {
        chatService.deleteOrphanUserMessage(sessionId);
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
