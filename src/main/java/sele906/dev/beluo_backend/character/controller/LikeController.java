package sele906.dev.beluo_backend.character.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.character.service.LikeService;

@RestController
@RequestMapping("/api/character/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    //좋아요 추가
    @PostMapping("/{id}")
    public ResponseEntity<Void> addLike(@PathVariable String id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        likeService.addLike(auth.getName(), id);
        return ResponseEntity.ok().build();
    }

    //좋아요 삭제
    @DeleteMapping("/{id}")
    public void cancelLike(@PathVariable String id, Authentication auth) {
        if (auth != null) {
            likeService.cancelLike(auth.getName(), id);
        }
    }
}
