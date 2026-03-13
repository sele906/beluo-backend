package sele906.dev.beluo_backend.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.auth.domain.User;
import sele906.dev.beluo_backend.auth.dto.TokenResponse;
import sele906.dev.beluo_backend.auth.repository.UserRepository;
import sele906.dev.beluo_backend.auth.service.AuthService;
import sele906.dev.beluo_backend.auth.service.JwtService;
import sele906.dev.beluo_backend.exception.DataAccessException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 테스트 로그인 - 토큰 발급
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletResponse response) {

        TokenResponse tokens = authService.login(user);

        // 쿠키에 토큰 담기
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(".beluo.site")
                .path("/")
                .maxAge(3600)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(".beluo.site")
                .path("/")
                .maxAge(604800)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

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
                .domain(".beluo.site")
                .path("/")
                .maxAge(3600)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(".beluo.site")
                .path("/")
                .maxAge(604800)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "토큰 재발급 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        authService.logout(authentication);

        // 세션 무효화
        request.getSession().invalidate();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(".beluo.site")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(".beluo.site")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody User user) {
        authService.join(user);
        return ResponseEntity.ok(Map.of("message", "회원가입 완료"));
    }

}
