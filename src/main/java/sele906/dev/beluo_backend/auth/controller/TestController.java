package sele906.dev.beluo_backend.auth.controller;

import jakarta.annotation.PostConstruct;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/test")
    public String test() {
        return "success";
    }

    @GetMapping("/testLogin")
    public String testLogin() {
        return "success";
    }


    // 테스트 로그인 - 토큰 발급
    @PostMapping("/testLogin")
    public ResponseEntity<?> testLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {

        //db에서 사용자 확인 필요
        User user = userRepository.findByEmail(body.get("email"))
                .orElseThrow(() -> new DataAccessException("존재하지 않는 유저입니다"));

        //비밀번호 검증
        if (!passwordEncoder.matches(body.get("password"), user.getPassword())) {
            return ResponseEntity.status(401).body("비밀번호가 맞지 않습니다");
        }

        //JWT 발급
        String token = jwtService.generateToken(user.getId());

        // 쿠키에 토큰 담기
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)       // JS에서 접근 불가 (XSS 방어)
                .secure(true)         // HTTPS에서만 전송
                .sameSite("Lax")      // CSRF 방어
                .path("/")            // 모든 경로에 쿠키 전송
                .maxAge(3600)         // 1시간 (초 단위)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    @GetMapping("/testMe")
    public Map<String, String> testMe(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return Map.of("userId", userId, "message", "토큰 인증 성공!");
    }

    @PostConstruct
    public void logMongoInfo() {
        //테스트
        System.out.println("Mongo DB = " + mongoTemplate.getDb().getName());
        System.out.println("Collections = " + mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()));
    }
}
