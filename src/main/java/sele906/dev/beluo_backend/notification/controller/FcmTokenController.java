package sele906.dev.beluo_backend.notification.controller;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> registerToken(@RequestBody Map<String, String> body, Authentication auth) {

        String userId = auth.getName();
        String token = body.get("token");

        fcmTokenService.saveToken(userId, token);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/token")
    public ResponseEntity<Void> deleteToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        fcmTokenService.deleteToken(token);
        return ResponseEntity.noContent().build();
    }
}
