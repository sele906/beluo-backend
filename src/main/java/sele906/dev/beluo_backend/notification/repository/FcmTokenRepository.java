package sele906.dev.beluo_backend.notification.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.notification.domain.FcmToken;

import java.util.List;

public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {

    // 한 유저의 모든 기기 토큰 조회 (PC, 폰 등 여러 개)
    List<FcmToken> findByUserId(String userId);

    // 특정 토큰 존재 여부 (중복 저장 방지용)
    boolean existsByToken(String token);

    // 무효 토큰 삭제용 (FCM이 "죽은 토큰"이라고 응답할 때)
    void deleteByToken(String token);
}
