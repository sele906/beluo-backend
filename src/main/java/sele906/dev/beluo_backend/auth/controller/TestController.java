package sele906.dev.beluo_backend.auth.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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

    @GetMapping("/test")
    public String test(Authentication authentication) {

        System.out.println("auth type = " + authentication.getClass());
        System.out.println("name = " + authentication.getName());
        System.out.println("principal = " + authentication.getPrincipal());
        System.out.println("role = " + authentication.getAuthorities()); // ROLE_USER

        return "ok";
    }

    @PostConstruct
    public void logMongoInfo() {
        //테스트
        System.out.println("Mongo DB = " + mongoTemplate.getDb().getName());
        System.out.println("Collections = " + mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()));
    }
}
