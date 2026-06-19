package sele906.dev.beluo_backend.notification.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.notification.service.FcmTokenService;

import java.util.Map;

@RestController
@RequestMapping("/api/fcm")
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    public FcmTokenController(FcmTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    @PostMapping("/token")
    public Map<String, String> registerToken(@RequestBody Map<String, String> body, Authentication auth) {

        String userId = auth.getName();
        String token = body.get("token");

        fcmTokenService.saveToken(userId, token);

        return Map.of("result", "토큰 등록 완료");
    }
}
