package sele906.dev.beluo_backend.character.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class CharacterService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CharacterRepository characterRepository;

    public String createCharacter(Character character, MultipartFile file) throws IOException {

        Character c = new Character();
        c.setCreatedAt(Instant.now());

        c.setCharacterName(character.getCharacterName());
        c.setSummary(character.getSummary());
        c.setPersonality(character.getPersonality());
        c.setFirstMessage(character.getFirstMessage());
        c.setTag(character.getTag());

        // 파일 처리
        Map result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "character")
        );
        c.setCharacterImgUrl((String) result.get("secure_url"));

        try {
            Character saved = characterRepository.save(c);
            return saved.getId().toString();
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 세팅 저장 실패", e);
        }
    }

    public Map<String, Object> getCharacterList() {

        List<Character> recentCharacters;
        List<Character> popularCharacters;
        List<Character> likedCharacters;

        try {
            recentCharacters = characterRepository.requestRecentCharacters();
            popularCharacters = characterRepository.requestRecentCharacters();
            likedCharacters = characterRepository.requestRecentCharacters();
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 리스트 불러오기 실패", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("recent", recentCharacters != null ? recentCharacters : List.of());
        response.put("popular", popularCharacters != null ? popularCharacters : List.of());
        response.put("liked", likedCharacters != null ? likedCharacters : List.of());

        return response;
    }

    //캐릭터 상세정보
    public Character getCharacterDetail(String id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("캐릭터를 찾을 수 없습니다"));
    }
}
