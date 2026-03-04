package sele906.dev.beluo_backend.character.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import java.time.Instant;
import java.util.*;

@Service
public class CharacterService {

    @Autowired
    private CharacterRepository characterRepository;

    public String createCharacter() {
        //character 데이터 생성
        Character c = new Character();
        c.setCreatedAt(Instant.now());

        c.setCharacterName("connor"); //초기세팅
        c.setCharacterFilePath("/beluo/character/connor.jpg"); //초기세팅
        c.setCharacterThumbFilePath("/beluo/character/thumb/connor.jpg"); //초기세팅
        c.setPersonality("코너는 디트로이트 비컴 휴먼에 나오는 친절한 안드로이드이다"); //초기세팅
        c.setFirstMessage("안녕하세요, 저는 사이버라이프에서 만들어진 안드로이드 코너입니다"); //초기세팅

        List<String> tag = new ArrayList<>(); //초기세팅
        tag.add("game");
        tag.add("android");

        c.setTag(tag);

        //db에 저장
        try {
            characterRepository.save(c);
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 세팅 저장 실패", e);
        }

        return "success";
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
