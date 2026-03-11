package sele906.dev.beluo_backend.auth.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.auth.domain.User;
import sele906.dev.beluo_backend.auth.repository.UserRepository;
import sele906.dev.beluo_backend.auth.service.JwtService;
import sele906.dev.beluo_backend.character.service.CharacterService;
import sele906.dev.beluo_backend.chat.service.ChatService;
import sele906.dev.beluo_backend.chat.service.ConversationService;
import sele906.dev.beluo_backend.exception.DataAccessException;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/tmpPassword")
    public String tmpPassword() {
        return passwordEncoder.encode("1234");
    }

    @PostConstruct
    public void logMongoInfo() {
        //테스트
        System.out.println("Mongo DB = " + mongoTemplate.getDb().getName());
        System.out.println("Collections = " + mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()));
    }
}
