package sele906.dev.beluo_backend.character.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.character.repository.CharacterRepositoryCustom;
import sele906.dev.beluo_backend.chat.domain.Conversation;
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

    public JSONObject getCharacterList() {

        List<Character> recentCharacters = characterRepository.requestRecentCharacters();
        List<Character> popularCharacters = characterRepository.requestRecentCharacters();
        List<Character> likedCharacters = characterRepository.requestRecentCharacters();

        JSONObject response = new JSONObject();
        response.put("recent", toJsonArray(recentCharacters));
        response.put("popular", toJsonArray(popularCharacters));
        response.put("liked", toJsonArray(likedCharacters));

        return response;
    }

    private JSONArray toJsonArray(List<Character> characters) {
        JSONArray arr = new JSONArray();
        for (Character c : characters) {
            JSONObject obj = new JSONObject();
            obj.put("id", c.getId());
            obj.put("characterName", c.getCharacterName());
            obj.put("personality", c.getPersonality());
            obj.put("tag", c.getTag());
            arr.put(obj);
        }
        return arr;
    }

    //캐릭터 상세정보
    public Character getCharacterDetail(String id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("캐릭터를 찾을 수 없습니다"));
    }
}
