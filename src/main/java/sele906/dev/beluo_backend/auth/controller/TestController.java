package sele906.dev.beluo_backend.auth.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.character.service.CharacterService;
import sele906.dev.beluo_backend.chat.service.ChatService;
import sele906.dev.beluo_backend.chat.service.ConversationService;

import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/testLogin")
    public String testLogin() {
        return "success";
    }

    @PostMapping("/testLogin")
    public String testLogin(String id, String pwd) {
        System.out.println(id);
        System.out.println(pwd);
        return "success";
    }

    @PostConstruct
    public void logMongoInfo() {
        //테스트
        System.out.println("Mongo DB = " + mongoTemplate.getDb().getName());
        System.out.println("Collections = " + mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()));
    }
}
