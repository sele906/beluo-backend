package sele906.dev.beluo_backend.character.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.service.CharacterService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    //캐릭터 생성
    @GetMapping("/create")
    public String createCharacter() {
        String response = characterService.createCharacter();
        return response;
    }

    //최근 10개 캐릭터 출력
    @GetMapping("/list")
    public Map<String, Object> getCharacterList() {
        return characterService.getCharacterList();
    }

    //캐릭터 상세 페이지
    @GetMapping("/detail")
    public Character characterDetail(@RequestParam String id) {
        return characterService.getCharacterDetail(id);
    }
}
