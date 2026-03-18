package sele906.dev.beluo_backend.mypage.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.character.domain.Blocked;
import sele906.dev.beluo_backend.character.domain.Like;
import sele906.dev.beluo_backend.character.repository.BlockedRepository;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.character.repository.LikeRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.UserRepository;
import sele906.dev.beluo_backend.character.domain.Character;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyPageService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CharacterRepository characterRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    BlockedRepository blockedRepository;

    public Map<String, Object> overView(String userId) {

        try {
            Map<String, Object> map = new HashMap<>();

            //차단된 캐릭터 거르기
            List<String> blockedIds = List.of();
            if (userId != null) {
                blockedIds = blockedRepository.findByUserId(userId).stream()
                        .map(Blocked::getCharacterId)
                        .toList();
            }

            //사용자 정보
            User user = userRepository.userOverview(userId);

            //내가 만든 캐릭터
            List<Character> createdCharacters = characterRepository.requestCreatedCharacters(userId);

            //내가 관심있는 캐릭터
            List<Character> likedCharacters = new ArrayList<>();

            if (userId != null) {

                //차단된 캐릭터 거르기
                List<String> finalBlockedIds = blockedIds;
                List<String> characterIds = likeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(Like::getCharacterId)
                        .filter(id -> !finalBlockedIds.contains(id))
                        .limit(10)
                        .toList();

                //관심있는 캐릭터 리스트 출력
                Map<String, Character> characterMap = characterRepository.requestLikedCharacters(characterIds).stream()
                        .collect(java.util.stream.Collectors.toMap(c -> c.getId().toString(), c -> c));
                likedCharacters = characterIds.stream()
                        .filter(characterMap::containsKey)
                        .map(characterMap::get)
                        .toList();
            }

            map.put("info", user);
            map.put("created", createdCharacters);
            map.put("liked", likedCharacters);

            return map;

        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("마이페이지 불러오기 실패", e);
        }
    }

    public User profile(String userId) {

        try {
            User user = userRepository.userDetail(userId);
            if (user == null) {
                throw new InvalidRequestException("회원정보를 찾을 수 없습니다");
            }
            return user;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("마이페이지 불러오기 실패", e);
        }
    }

    public List<Character> characters(String userId) {

        try {
            return characterRepository.createdCharacters(userId);
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("제작한 캐릭터 목록 불러오기 실패", e);
        }
    }

    //무한 스크롤 제한!!
    public List<Character> liked(String userId) {
        try {

            //차단된 캐릭터 거르기
            List<String> blockedIds = blockedRepository.findByUserId(userId).stream()
                    .map(Blocked::getCharacterId)
                    .toList();

            List<String> characterIds = likeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .map(Like::getCharacterId)
                    .filter(id -> !blockedIds.contains(id))
                    .toList();

            //관심있는 캐릭터 리스트 출력
            Map<String, Character> characterMap = characterRepository.requestLikedCharacters(characterIds).stream()
                    .collect(java.util.stream.Collectors.toMap(c -> c.getId().toString(), c -> c));

            return characterIds.stream()
                    .filter(characterMap::containsKey)
                    .map(characterMap::get)
                    .toList();

        } catch (Exception e) {
            throw new DataAccessException("관심있는 캐릭터 목록 불러오기 실패", e);
        }
    }

    //무한 스크롤 제한!!
    public List<Character> blocked(String userId) {
        try {

            List<String> blockedIds = blockedRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .map(Blocked::getCharacterId)
                    .toList();

            //차단된 캐릭터 리스트 출력
            Map<String, Character> characterMap = characterRepository.requestBlockedCharacters(blockedIds).stream()
                    .collect(java.util.stream.Collectors.toMap(c -> c.getId().toString(), c -> c));

            return blockedIds.stream()
                    .filter(characterMap::containsKey)
                    .map(characterMap::get)
                    .toList();
        } catch (Exception e) {
            throw new DataAccessException("차단된 캐릭터 목록 불러오기 실패", e);
        }
    }

    //캐릭터 상세정보
    public Character getCharacterDetail(String id) {
        try {
            Character character = characterRepository.findById(id)
                    .orElseThrow(() -> new InvalidRequestException("캐릭터를 찾을 수 없습니다"));

            return character;
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 상세정보 불러오기 실패");
        }
    }

    public void getCharacterDelete(String id, String userId) {

        try {
            characterRepository.deleteByIdAndUserId(id, userId);
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 삭제 실패", e);
        }
    }

    public void getCharacterEdit(String characterId, Character character, MultipartFile file, String userId) throws IOException {

        Character c = new Character();
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
            c.setCharacterImgUrl(character.getCharacterImgUrl());
        }

        c.setPublic(c.isPublic());

        try {
            characterRepository.updateByIdAndUserId(characterId, userId, c);
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 업데이트 실패", e);
        }
    }

    public void getProfileEdit(String userId, User user, MultipartFile file) throws IOException {
        User u = new User();
        u.setName(user.getName());
        u.setPassword(user.getPassword());
        u.setUserImgUrl(user.getUserImgUrl());

        // 파일 처리
        if (file != null && !file.isEmpty()) {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "character")
            );
            u.setUserImgUrl((String) result.get("secure_url"));
        } else {
            u.setUserImgUrl(user.getUserImgUrl());
        }

        try {
            userRepository.updateById(userId, u);
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 업데이트 실패", e);
        }
    }
}
