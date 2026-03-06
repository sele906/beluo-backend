package sele906.dev.beluo_backend.character.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.service.CharacterService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    //캐릭터 생성
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createCharacter(
            @RequestPart("character") Character character,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return characterService.createCharacter(character, file);
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
