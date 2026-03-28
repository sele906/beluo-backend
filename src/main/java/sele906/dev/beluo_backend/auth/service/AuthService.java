package sele906.dev.beluo_backend.auth.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.credit.service.CreditService;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.auth.dto.TokenResponse;
import sele906.dev.beluo_backend.user.repository.user.UserRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CreditService creditService;

    public TokenResponse login(User user) {

        //db에서 사용자 확인 필요
        User u = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new DataAccessException("존재하지 않는 유저입니다"));

        //비밀번호 검증
        if (!passwordEncoder.matches(user.getPassword(), u.getPassword())) {
            throw new DataAccessException("비밀번호가 맞지 않습니다");
        }

        //JWT 발급
        String accessToken = jwtService.generateAccessToken(u.getId(), u.getRole());
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
        String newAccessToken = jwtService.generateAccessToken(userId, user.getRole());
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

    public void logout(Authentication authentication) {

        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다"));

        user.setRefreshToken(null);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new DataAccessException("Refresh 토큰 삭제 실패", e);
        }
    }

    public void join(User user, MultipartFile file) throws IOException {

        User u = new User();
        u.setEmail(user.getEmail());
        u.setPassword(passwordEncoder.encode(user.getPassword()));
        u.setName(user.getName());
        u.setCreatedAt(Instant.now());
        u.setBirth(user.getBirth());

        // 프로필 사진 업로드
        if (file != null && !file.isEmpty()) {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "profile")
            );
            u.setUserImgUrl((String) result.get("secure_url"));
        } else {
            u.setUserImgUrl("https://res.cloudinary.com/dncvqdlih/image/upload/v1774283989/blank_user_t5mdgv.jpg");
        }

        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new DataAccessException("회원가입 실패", e);
        }

        creditService.grantFreeBeta(u.getId());
    }

    // 인증 코드 발송
    public void verifyEmail(String email) {

        String code = String.format("%06d", new Random().nextInt(1000000));

        redisTemplate.opsForValue().set("verify:" + email, code,5, TimeUnit.MINUTES);


        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("[BELUO] 이메일 인증 코드");
            mail.setText("인증 코드: " + code + "\n\n5분 내에 입력해 주세요.");
            mailSender.send(mail);
        } catch (Exception e) {
            throw new InvalidRequestException("메일 발송에 실패했습니다.");
        }
    }

    // 인증 코드 확인
    public void checkVerity(String email, String code) {
        String saved = redisTemplate.opsForValue().get("verify:" + email);

        if (saved == null) {
            throw new InvalidRequestException("인증 코드를 먼저 요청해 주세요.");
        }

        if (!saved.equals(code)) {
            throw new InvalidRequestException("인증 코드가 올바르지 않습니다.");
        }

        redisTemplate.delete("verify:" + email);
    }
}
