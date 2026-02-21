package sele906.dev.beluo_backend.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.chat.service.ChatService;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

//    @PostMapping
//    public Map<String, String> chat(@RequestBody Map<String, String> body) {
//        String userMessage = body.get("message");
//        String reply = chatService.sendChatApi(userMessage);
//
//        return Map.of("reply", reply);
//    }


}
