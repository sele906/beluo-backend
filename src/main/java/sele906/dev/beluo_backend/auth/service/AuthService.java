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

import java.time.Instant;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    public TokenResponse login(User user) {

        //db에서 사용자 확인 필요
        User u = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new DataAccessException("존재하지 않는 유저입니다"));

        //비밀번호 검증
        if (!passwordEncoder.matches(user.getPassword(), u.getPassword())) {
            throw new DataAccessException("비밀번호가 맞지 않습니다");
        }

        //JWT 발급
        String accessToken = jwtService.generateAccessToken(u.getId());
        String refreshToken = jwtService.generateRefreshToken(u.getId());

        //user db에 저장
        u.setRefreshToken(refreshToken);
        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new DataAccessException("Refresh 토큰 저장 실패", e);
        }

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {

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

        //JWT 발급
        String newAccessToken = jwtService.generateAccessToken(userId);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        //user db에 저장
        user.setRefreshToken(newRefreshToken);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new DataAccessException("Refresh 토큰 저장 실패", e);
        }

        //new Token 발급
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다"));

        user.setRefreshToken(null);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new DataAccessException("Refresh 토큰 삭제 실패", e);
        }
    }

    public void join(User user) {

        User u = new User();
        u.setEmail(user.getEmail());
        u.setPassword(passwordEncoder.encode(user.getPassword()));
        u.setName(user.getName());
        u.setCreatedAt(Instant.now());

        System.out.println("유저 정보: " + u);

        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new DataAccessException("회원가입 실패", e);
        }
    }


}
