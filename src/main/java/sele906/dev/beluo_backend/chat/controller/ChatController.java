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

        //유저 메세지 db 저장
        chatService.chatDataSave(userRole, userContent, sessionId);
        chatService.afterSummaryChatCount(sessionId);

        //프롬프트에 최근 대화 합쳐서 api 보내기 (ai 답변은 저장 X, confirm에서 저장)
        String reply = chatService.sendChatApi(sessionId);

        return Map.of("reply", reply);
    }

    //다시 생성 (db 저장 없이 ai 답변만 생성, db 마지막이 유저 메세지라 컨텍스트 동일)
    @PostMapping("/regenerate")
    public Map<String, String> chatRegenerate(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        String reply = chatService.sendChatApi(sessionId);
        return Map.of("reply", reply);
    }

    //선택된 답변 DB 저장
    @PostMapping("/confirm")
    public void chatConfirm(@RequestBody Map<String, String> body) {

        String sessionId = body.get("sessionId");
        String aiRole = "assistant";
        String reply = body.get("reply");
        String aiContent = reply;

        //ai 메세지 db 저장
        chatService.chatDataSave(aiRole, aiContent, sessionId);
        chatService.afterSummaryChatCount(sessionId);
    }

    //메세지 출력
    @GetMapping("/messages")
    public Map<String, Object> getMessageList(
            @RequestParam String sessionId,
            @RequestParam(required = false) String before
    ) {
        return chatService.requestRecentChat(sessionId, before);
    }

}
