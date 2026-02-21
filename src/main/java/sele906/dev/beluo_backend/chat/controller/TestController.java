package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.ChatService;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/testChatSend")
    public List<Message> testChatSend(@RequestBody Map<String, String> body) {

        //정보 가져오기
        String userMessage = body.get("message");
        String chatRoomNum = "chatRoomNum";

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

    @GetMapping("/testCreateChatRoom")
    public String testCreateChatRoom() {
        //시스템 프롬프트
        //빈 요약 프롬프트 만들기
        //고유한 챗방 세션 생성
        return "";
    }

    //최근 10개 대화 출력
    @GetMapping("/testFind")
    public List<Message> testFind() {

        String chatRoomNum = "chatRoomNum";
        return chatService.requestRecentChat(chatRoomNum);
    }
}
