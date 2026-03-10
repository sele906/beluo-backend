package sele906.dev.beluo_backend.auth.service;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.auth.domain.User;
import sele906.dev.beluo_backend.auth.dto.TokenResponse;
import sele906.dev.beluo_backend.auth.repository.UserRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {
//    AuthService
//  ├── login(email, password) → TokenResponse (accessToken, refreshToken 반환)
//  ├── refresh(refreshToken) → 새 accessToken 반환
//  └── logout(userId) → DB에서 refreshToken 삭제

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    public TokenResponse login(String email, String password) {

        //db에서 사용자 확인 필요
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 유저입니다"));

        //비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new DataAccessException("비밀번호가 맞지 않습니다");
        }

        //JWT 발급
        String accessToken = jwtService.generateAccessToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        //user db에 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    public String refresh(String refreshToken) {

        //쿠키 없으면 거절
        if (refreshToken == null) {
            throw new InvalidRequestException("refresh Token 없음");
        }

        //토큰 유효한지 확인
        if (!jwtService.isValid(refreshToken)) {
            throw new InvalidRequestException("refresh Token 만료 또는 취소");
        }

        //토큰에서 userId 꺼내기
        String userId = jwtService.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다"));

        //db에 저장된 refreshToken이랑 일치하는지 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidRequestException("유효하지 않은 refresh Token");
        }

        //new Access Token 발급
        return jwtService.generateAccessToken(userId);
    }

    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다"));

        user.setRefreshToken(null);
        userRepository.save(user);

    }
}
