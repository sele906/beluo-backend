package sele906.dev.beluo_backend.character.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.character.domain.Blocked;
import sele906.dev.beluo_backend.character.repository.BlockedRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;

import java.time.Instant;

@Service
public class BlockedService {

    @Autowired
    BlockedRepository blockedRepository;

    //차단 추가
    public void addBlocked(String userId, String characterId) {

        Blocked blocked = new Blocked();
        blocked.setUserId(userId);
        blocked.setCharacterId(characterId);
        blocked.setCreatedAt(Instant.now());

        try {
            blockedRepository.save(blocked);
        } catch (Exception e) {
            throw new DataAccessException("차단할 캐릭터 목록에 저장할 수 없습니다. 다시 시도해 주세요");
        }
    }

    //차단 해제
    public void cancelBlocked(String userId, String characterId) {
        try {
            blockedRepository.deleteByUserIdAndCharacterId(userId, characterId);
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 차단 해제에 실패했습니다. 다시 시도해 주세요");
        }
    }
}
