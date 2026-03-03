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
        String reply = chatService.sendChatApi(userMessage, sessionId);

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

    //최근 10개 대화 출력
    @GetMapping("/messages")
    public List<Message> getMessageList(@RequestParam String sessionId) {
        return chatService.requestRecentChat(sessionId);
    }

}
