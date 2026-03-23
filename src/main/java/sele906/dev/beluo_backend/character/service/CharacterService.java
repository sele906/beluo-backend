package sele906.dev.beluo_backend.character.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.ai.client.OpenAiClient;
import sele906.dev.beluo_backend.character.domain.Blocked;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.domain.PersonalityJson;
import sele906.dev.beluo_backend.character.repository.BlockedRepository;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.character.domain.Like;
import sele906.dev.beluo_backend.character.repository.LikeRepository;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class CharacterService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private OpenAiClient openAiClient;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BlockedRepository blockedRepository;

    @Autowired
    private CharacterCacheService characterCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    //캐릭터 overview
    public Map<String, Object> getCharacterOverviewList(String userId) {

        try {
            // 1단계: blocked, likeList 병렬 실행
            CompletableFuture<List<String>> blockedFuture = CompletableFuture.supplyAsync(() -> {
                if (userId == null) return List.of();
                return blockedRepository.findByUserId(userId).stream()
                        .map(Blocked::getCharacterId)
                        .toList();
            });

            CompletableFuture<List<String>> likeIdsFuture = CompletableFuture.supplyAsync(() -> {
                if (userId == null) return List.of();
                return likeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(Like::getCharacterId)
                        .toList();
            });

            CompletableFuture.allOf(blockedFuture, likeIdsFuture).join();
            List<String> blockedIds = blockedFuture.get();
            List<String> likeIds = likeIdsFuture.get();

            // 2단계: recent(캐시), popular(캐시), likedCharacters 병렬 실행
            CompletableFuture<List<Character>> recentFuture = CompletableFuture.supplyAsync(() -> {
                List<String> finalBlockedIds = blockedIds;
                return characterCacheService.getRecentCharacters().stream()
                        .filter(c -> !finalBlockedIds.contains(c.getId().toString()))
                        .toList();
            });

            CompletableFuture<List<Character>> popularFuture = CompletableFuture.supplyAsync(() -> {
                List<String> finalBlockedIds = blockedIds;
                return characterCacheService.getPopularCharacters().stream()
                        .filter(c -> !finalBlockedIds.contains(c.getId().toString()))
                        .toList();
            });

            CompletableFuture<List<Character>> likedFuture = CompletableFuture.supplyAsync(() -> {
                if (userId == null) return List.of();
                List<String> characterIds = likeIds.stream()
                        .filter(id -> !blockedIds.contains(id))
                        .limit(10)
                        .toList();
                Map<String, Character> characterMap = characterRepository.findLikedCharacters(characterIds).stream()
                        .collect(java.util.stream.Collectors.toMap(c -> c.getId().toString(), c -> c));
                return characterIds.stream()
                        .filter(characterMap::containsKey)
                        .map(characterMap::get)
                        .toList();
            });

            CompletableFuture.allOf(recentFuture, popularFuture, likedFuture).join();

            Map<String, Object> response = new HashMap<>();
            response.put("recent", recentFuture.get());
            response.put("popular", popularFuture.get());
            response.put("liked", likedFuture.get());

            return response;

        } catch (Exception e) {
            throw new DataAccessException("캐릭터 리스트 불러오기 실패", e);
        }
    }

    //캐릭터 검색
    public Map<String, Object> getCharacterList(String userId, String keyword) {
        try {
            List<String> blockedIds = List.of();
            if (userId != null) {
                blockedIds = blockedRepository.findByUserId(userId).stream()
                        .map(Blocked::getCharacterId)
                        .toList();
            }

            List<Character> characters = characterRepository.searchCharacters(keyword, blockedIds);

            Map<String, Object> response = new HashMap<>();
            response.put("characters", characters);
            return response;

        } catch (Exception e) {
            throw new DataAccessException("캐릭터 목록 불러오기 실패", e);
        }
    }

    //캐릭터 요약 상세정보
    public Map<String, Object> getCharacterSummaryDetail(String id, String userId) {

        try {
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

        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 상세정보 불러오기 실패", e);
        }
    }

    //캐릭터 추가하기
    public String createCharacter(Character character, MultipartFile file, String userId) throws IOException {

        Character c = new Character();
        c.setCreatedAt(Instant.now());

        c.setCharacterName(character.getCharacterName());
        c.setSummary(character.getSummary());
        c.setPersonality(character.getPersonality());
        c.setFirstMessage(character.getFirstMessage());
        c.setTag(character.getTag());

        // 파일 처리
        if (file != null && !file.isEmpty()) {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "character")
            );
            c.setCharacterImgUrl((String) result.get("secure_url"));
        } else {
            c.setCharacterImgUrl(null);
        }

        //성격 json으로 변환
        c.setPersonalityJson(createPersonality(character.getPersonality()));

        c.setUserId(userId);

        c.setPublic(character.isPublic());
        c.setLikeCount(0);
        c.setConvCount(0);

        try {
            Character saved = characterRepository.save(c);
            characterCacheService.evictCache();
            return saved.getId().toString();
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 세팅 저장 실패", e);
        }
    }

    //캐릭터 성격 json으로 정리
    public PersonalityJson createPersonality(String personalityString) {

        //예외처리
        if (personalityString == null) {
            throw new DataAccessException("캐릭터 성격 확인 불가");
        }

        //json 변환 프롬프트 작성
        Map<String, String> personalityJsonPrompt =  Map.of(
                "role", "system",
                "content", """PROMPT_REMOVED""" + personalityString + "}"
        );

        //캐릭터 프롬프트 출력
        String response = openAiClient.personality(personalityJsonPrompt);

        // 응답에서 JSON만 추출
        String raw = response.trim();
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        String jsonOnly = raw.substring(start, end + 1);

        try {
            return objectMapper.readValue(jsonOnly, PersonalityJson.class);
        } catch (Exception e) {
            throw new DataAccessException("personalityJson 파싱 실패", e);
        }
    }


}
