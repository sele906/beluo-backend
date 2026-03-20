package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.ChatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    //메세지 보내기
    @PostMapping("/send")
    public Map<String, String> chatSend(@RequestBody Map<String, String> body) {

        //정보 가져오기
        String userMessage = body.get("message");
        String sessionId = body.get("sessionId");

        //유저 메세지 db에 저장
        //role
        String userRole = "user";

        //content
        String userContent = userMessage;

        //db 저장
        chatService.chatDataSave(userRole, userContent, sessionId);
        chatService.afterSummaryChatCount(sessionId);

        //프롬프트에 최근 대화 합쳐서 api 보내기
        String reply = chatService.sendChatApi(sessionId);

        //응답 메세지 db에 저장
        //role
        String aiRole = "assistant";

        //content
        String aiContent = reply;

        //db 저장
        chatService.chatDataSave(aiRole, aiContent, sessionId);
        chatService.afterSummaryChatCount(sessionId);

        return Map.of("reply", aiContent);
    }

    //메세지 출력
    @GetMapping("/messages")
    public Map<String, Object> getMessageList(
            @RequestParam String sessionId,
            @RequestParam(required = false) String before
    ) {
        return chatService.requestRecentChat(sessionId, before);
    }

//    POST /api/chat/send     → 유저 메시지 저장 + 첫 AI 답변 반환
//    POST /api/chat/regenerate  → 같은 컨텍스트로 AI 답변 1개 더 반환
//    POST /api/chat/confirm  → 선택된 답변 DB 저장

}
