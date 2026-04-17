package sele906.dev.beluo_backend.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.auth.dto.TokenResponse;
import sele906.dev.beluo_backend.auth.service.AuthService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 로그인 > 토큰 발급
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletResponse response) {

        TokenResponse tokens = authService.login(user);

        // 쿠키에 토큰 담기
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(3600)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(2592000)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    //게스트 로그인
    @PostMapping("/guest")
    public ResponseEntity<?> guestLogin(HttpServletResponse response) {

        String accessToken = authService.guestLogin();

        // 쿠키에 토큰 담기
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(86400)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {

        //쿠키에서 refreshToken 꺼내기
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        TokenResponse tokens = authService.refresh(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(3600)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(2592000)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "토큰 재발급 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        if (authentication != null) {
            authService.logout(authentication);
        }

        // 세션 무효화
        request.getSession().invalidate();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @PostMapping(value = "/join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> join(
            @RequestPart("user") User user,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        authService.join(user, file);
        return ResponseEntity.ok(Map.of("message", "회원가입 완료"));
    }

    // 인증 코드 발송
    @PostMapping("/verify/send")
    public ResponseEntity<?> verifySend(@RequestBody Map<String, String> body) {
        authService.verifyEmail(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "인증 코드가 발송됐습니다"));
    }

    // 인증 코드 확인
    @PostMapping("/verify/check")
    public ResponseEntity<?> verifyCheck(@RequestBody Map<String, String> body) {
        authService.checkVerity(body.get("email"), body.get("code"));
        return ResponseEntity.ok(Map.of("message", "이메일 인증 완료"));
    }

    @PostMapping(value = "/oauth2/join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> oauthJoin(
            Authentication authentication,
            @RequestParam("name") String name,
            @RequestParam("birth") String birth,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        authService.oauthJoin(authentication.getName(), name, birth, file);
        return ResponseEntity.ok(Map.of("message", "회원가입 완료"));
    }
}
