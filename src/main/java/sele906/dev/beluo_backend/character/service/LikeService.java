package sele906.dev.beluo_backend.character.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.character.domain.Like;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.character.repository.LikeRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;

import java.time.Instant;
import java.util.List;

@Service
public class LikeService {

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    CharacterRepository characterRepository;

    //좋아요 추가
    public void addLike(String userId, String characterId) {

        boolean liked = likeRepository.existsByUserIdAndCharacterId(userId, characterId);

        if (liked) {
            return;
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setCharacterId(characterId);
        like.setCreatedAt(Instant.now());

        try {
            likeRepository.save(like);
            characterRepository.increaseLikeCount(characterId);
        } catch (Exception e) {
            throw new DataAccessException("관심있는 캐릭터로 저장할 수 없습니다.");
        }
    }

    //좋아요 취소
    public void cancelLike(String userId, String characterId) {

        boolean liked = likeRepository.existsByUserIdAndCharacterId(userId, characterId);

        if (!liked) {
            return;
        }

        try {
            likeRepository.deleteByUserIdAndCharacterId(userId, characterId);
            characterRepository.decreaseLikeCount(characterId);
        } catch (Exception e) {
            throw new DataAccessException("관심있는 캐릭터 삭제에 실패했습니다.");
        }
    }
}
