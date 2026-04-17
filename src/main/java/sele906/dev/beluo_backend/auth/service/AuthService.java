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
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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
                .orElseThrow(() -> new InvalidRequestException("아이디를 확인해주세요"));

        //비밀번호 검증
        if (!passwordEncoder.matches(user.getPassword(), u.getPassword())) {
            throw new InvalidRequestException("비밀번호를 확인해주세요");
        }

        //JWT 발급
        String accessToken = jwtService.generateAccessToken(u.getId(), u.getRole());
        String refreshToken = jwtService.generateRefreshToken(u.getId());

        //user db에 저장
        u.setRefreshToken(refreshToken);
        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new DataAccessException("Refresh Token 저장에 실패했습니다", e);
        }

        return new TokenResponse(accessToken, refreshToken);
    }

    public String guestLogin() {

        String guestId = "guest_" + UUID.randomUUID();

        User u = new User();
        u.setId(guestId);
        u.setEmail(guestId + "@guest.local"); // unique index 충돌 방지
        u.setName("Guest");
        u.setCreatedAt(Instant.now());
        u.setRole("GUEST");
        u.setUserImgUrl("https://res.cloudinary.com/dncvqdlih/image/upload/v1774283989/blank_user_t5mdgv.jpg");
        u.setGuestExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new DataAccessException("게스트 계정 생성에 실패했습니다. 다시 시도해 주세요", e);
        }

        //JWT 발급
        String accessToken = jwtService.generateAccessToken(u.getId(), u.getRole());

        creditService.grantGuestFreeBeta(u.getId());

        return accessToken;
    }

    public TokenResponse refresh(String refreshToken) {

        //쿠키 없으면 거절
        if (refreshToken == null) {
            throw new InvalidRequestException("Refresh Token이 없습니다");
        }

        //토큰 유효한지 확인
        if (!jwtService.isValid(refreshToken)) {
            throw new InvalidRequestException("refresh Token이 만료 또는 취소되었습니다");
        }

        //토큰에서 userId 꺼내기
        String userId = jwtService.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다"));

        //db에 저장된 refreshToken이랑 일치하는지 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidRequestException("유효하지 않은 Refresh Token 입니다");
        }

        //JWT 발급
        String newAccessToken = jwtService.generateAccessToken(userId, user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        //user db에 저장
        user.setRefreshToken(newRefreshToken);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new DataAccessException("Refresh Token 저장에 실패했습니다", e);
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
            throw new DataAccessException("Refresh Token 삭제에 실패했습니다", e);
        }
    }

    public void join(User user, MultipartFile file) throws IOException {

        User u = new User();
        u.setEmail(user.getEmail());
        u.setPassword(passwordEncoder.encode(user.getPassword()));
        u.setName(user.getName());
        u.setCreatedAt(Instant.now());
        u.setBirth(user.getBirth());
        u.setRole("USER");

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
            throw new DataAccessException("회원가입에 실패했습니다. 다시 시도해 주세요", e);
        }

        creditService.grantFreeBeta(u.getId());
    }

    public void oauthJoin(String userId, String name, String birth, MultipartFile file) throws IOException {
        //유저 정보 가져오기
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("존재하지 않는 사용자입니다"));

        //유저 정보 업데이트
        u.setBirth(birth);
        u.setName(name);

        // 프로필 사진 업로드
        if (file != null && !file.isEmpty()) {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "user")
            );
            u.setUserImgUrl((String) result.get("secure_url"));
        }

        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new DataAccessException("추가 회원정보 저장에 실패했습니다. 다시 시도해 주세요", e);
        }
    }

    // 인증 코드 발송
    public void verifyEmail(String email) {

        // 이메일 형식 확인
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new InvalidRequestException("유효하지 않은 이메일 형식입니다");
        }

        // 중복 이메일 확인
        if (userRepository.findByEmail(email).isPresent()) {
            throw new InvalidRequestException("이미 사용 중인 이메일입니다");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));

        redisTemplate.opsForValue().set("verify:" + email, code,5, TimeUnit.MINUTES);

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("[BELUO] 이메일 인증 코드");
            mail.setText("인증 코드: " + code + "\n\n5분 내에 입력해 주세요.");
            mailSender.send(mail);
        } catch (Exception e) {
            throw new InvalidRequestException("메일 발송에 실패했습니다. 다시 시도해 주세요");
        }
    }

    // 인증 코드 확인
    public void checkVerity(String email, String code) {
        String saved = redisTemplate.opsForValue().get("verify:" + email);

        if (saved == null) {
            throw new InvalidRequestException("인증 코드를 먼저 요청해 주세요");
        }

        if (!saved.equals(code)) {
            throw new InvalidRequestException("인증 코드가 올바르지 않습니다");
        }

        redisTemplate.delete("verify:" + email);
    }



}
