package sele906.dev.beluo_backend.character.controller;

import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.service.CharacterService;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.service.ChatService;
import sele906.dev.beluo_backend.chat.service.ConversationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private MongoTemplate mongoTemplate;

//    //캐릭터 생성
//    @GetMapping("/testCreateCharacter")
//    public String testCreateCharacter() {
//        String response = characterService.createCharacter();
//        return response;
//    }
//
//    //최근 10개 캐릭터 출력
//    @GetMapping("/testGetCharacterList")
//    public JSONObject testGetConversationList() {
//        return characterService.getCharacterList();
//    }

//    @PostMapping("/testChatSend")
//    public Map<String, String> testChatSend(@RequestBody Map<String, String> body) {
//
//        //정보 가져오기
//        String userMessage = body.get("message");
//        String sessionId = body.get("sessionId");
//
//        //유저 메세지 db에 저장
//        //role
//        String userRole = "user";
//
//        //content
//        String userContent = userMessage;
//
//        //db 저장
//        chatService.chatDataSave(userRole, userContent, sessionId);
//        chatService.afterSummaryChatCount(sessionId);
//
//        //프롬프트에 최근 대화 합쳐서 api 보내기
//        String reply = chatService.sendChatApi(userMessage, sessionId);
//
//        //응답 메세지 db에 저장
//        //role
//        String aiRole = "assistant";
//
//        //content
//        String aiContent = reply;
//
//        //db 저장
//        chatService.chatDataSave(aiRole, aiContent, sessionId);
//        chatService.afterSummaryChatCount(sessionId);
//
//        return Map.of("reply", aiContent);
//    }
//
//    //캐릭터 상세정보 페이지와 이어짐
//    @GetMapping("/testCreateConversation")
//    public String testCreateConversation() {
//        return conversationService.createConversation();
//    }
//
//    //최근 10개 채팅방 출력
//    @GetMapping("/testGetConversationList")
//    public List<Conversation> testGetConversationList() {
//        return conversationService.conversationList();
//    }
//
//    //최근 10개 대화 출력
//    @GetMapping("/testGetMessageList")
//    public List<Message> testGetMessageList(@RequestParam String sessionId) {
//        return chatService.requestRecentChat(sessionId);
//    }


//    //최근 10개 대화 출력
//    @GetMapping("/testFind")
//    public List<Message> testFind() {
//
//        String sessionId = "e53e79df-f476-4245-a3fc-4ffd31b63df6"; //초기세팅
//        return chatService.requestRecentChat(sessionId);
//    }

    @PostConstruct
    public void logMongoInfo() {
        //테스트
        System.out.println("Mongo DB = " + mongoTemplate.getDb().getName());
        System.out.println("Collections = " + mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()));
    }
}
