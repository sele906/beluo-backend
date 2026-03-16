package sele906.dev.beluo_backend.character.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.character.domain.Blocked;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.BlockedRepository;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.character.domain.Like;
import sele906.dev.beluo_backend.character.repository.LikeRepository;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class CharacterService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BlockedRepository blockedRepository;

    public String createCharacter(Character character, MultipartFile file, String userId) throws IOException {

        Character c = new Character();
        c.setCreatedAt(Instant.now());

        c.setCharacterName(character.getCharacterName());
        c.setSummary(character.getSummary());
        c.setPersonality(character.getPersonality());
        c.setFirstMessage(character.getFirstMessage());
        c.setTag(character.getTag());

        // 파일 처리
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("이미지 파일이 없습니다");
        }

        Map result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "character")
        );
        c.setCharacterImgUrl((String) result.get("secure_url"));

        c.setUserId(userId);

        c.setPublic(true);
        c.setLikeCount(0);
        c.setConvCount(0);

        try {
            Character saved = characterRepository.save(c);
            return saved.getId().toString();
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 세팅 저장 실패", e);
        }
    }

    public Map<String, Object> getCharacterList(String userId) {

        try {

            List<String> blockedIds = List.of();
            if (userId != null) {
                blockedIds = blockedRepository.findByUserId(userId).stream()
                        .map(Blocked::getCharacterId)
                        .toList();
            }

            List<Character> recentCharacters = characterRepository.requestRecentCharacters(blockedIds);
            List<Character> popularCharacters = characterRepository.requestPopularCharacters(blockedIds);

            List<Character> likedCharacters = List.of();

            if (userId != null) {
                List<String> finalBlockedIds = blockedIds;
                List<String> characterIds = likeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(Like::getCharacterId)
                        .filter(id -> !finalBlockedIds.contains(id))
                        .toList();
                Map<String, Character> characterMap = characterRepository.findAllById(characterIds).stream()
                        .collect(java.util.stream.Collectors.toMap(c -> c.getId().toString(), c -> c));
                likedCharacters = characterIds.stream()
                        .filter(characterMap::containsKey)
                        .map(characterMap::get)
                        .toList();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("recent", recentCharacters);
            response.put("popular", popularCharacters);
            response.put("liked", likedCharacters);

            return response;

        } catch (Exception e) {
            throw new DataAccessException("캐릭터 리스트 불러오기 실패", e);
        }
    }

    //캐릭터 상세정보
    public Map<String, Object> getCharacterDetail(String id, String userId) {

        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("캐릭터를 찾을 수 없습니다"));

        User author = userRepository.findById(character.getUserId())
                .orElseThrow(() -> new InvalidRequestException("작성자 정보를 찾을 수 없습니다"));

        boolean liked = false;

        if (userId != null) {
            liked = likeRepository.existsByUserIdAndCharacterId(userId, id);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("character", character);
        result.put("author", Map.of("name", author.getName()));
        result.put("liked", liked);

        return result;
    }

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

        likeRepository.save(like);
        characterRepository.increaseLikeCount(characterId);
    }

    //좋아요 취소
    public void cancelLike(String userId, String characterId) {

        boolean liked = likeRepository.existsByUserIdAndCharacterId(userId, characterId);

        if (!liked) {
            return;
        }

        likeRepository.deleteByUserIdAndCharacterId(userId, characterId);
        characterRepository.decreaseLikeCount(characterId);
    }

    //좋아요 여부 확인
    public boolean isLiked(String userId, String characterId) {
        return likeRepository.existsByUserIdAndCharacterId(userId, characterId);
    }

    //좋아요 리스트
    public List<Like> likeList(String userId) {
        return likeRepository.findByUserId(userId);
    }
}
