package sele906.dev.beluo_backend.notification.service;

import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.notification.domain.FcmToken;
import sele906.dev.beluo_backend.notification.repository.FcmTokenRepository;

import java.util.List;

@Service
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    public FcmTokenService(FcmTokenRepository fcmTokenRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
    }

    // 토큰 등록 (이미 있으면 무시 → 중복 방지)
    public void saveToken(String userId, String token) {
        if (fcmTokenRepository.existsByToken(token)) {
            return; // 이미 등록된 토큰이면 패스
        }
        fcmTokenRepository.save(new FcmToken(userId, token));
    }

    // 유저의 모든 기기 토큰 조회
    public List<FcmToken> getTokensByUserId(String userId) {
        return fcmTokenRepository.findByUserId(userId);
    }

    // 무효 토큰 삭제
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }
}
