package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.ChatService;
import sele906.dev.beluo_backend.chat.service.ConversationService;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversationService conversationService;

    @PostMapping("/testChatSend")
    public List<Message> testChatSend(@RequestBody Map<String, String> body) {

        //정보 가져오기
        String userMessage = body.get("message");
        String chatRoomNum = "c6a4b025-d994-4357-b183-376361c17da0";

        //유저 메세지 db에 저장
        //role
        String userRole = "user";

        //content
        String userContent = userMessage;

        //db 저장
        chatService.chatDataSave(userRole, userContent, chatRoomNum);
        chatService.afterSummaryChatCount(chatRoomNum);

        //프롬프트에 최근 대화 합쳐서 api 보내기
        String reply = chatService.sendChatApi(userMessage, chatRoomNum);

        //응답 메세지 db에 저장
        //role
        String aiRole = "assistant";

        //content
        String aiContent = reply;

        //db 저장
        chatService.chatDataSave(aiRole, aiContent, chatRoomNum);
        chatService.afterSummaryChatCount(chatRoomNum);

        //테스트용
        //최근 10개 대화 출력
        return chatService.requestRecentChat(chatRoomNum);

    }

    //캐릭터 상세정보 페이지와 이어짐
    @GetMapping("/testCreateConversation")
    public String testCreateConversation() {
        String response = conversationService.createConversation();
        System.out.println(response);
        return response;
    }

    //최근 10개 대화 출력
    @GetMapping("/testFind")
    public List<Message> testFind() {

        String chatRoomNum = "chatRoomNum";
        return chatService.requestRecentChat(chatRoomNum);
    }
}
