package sele906.dev.beluo_backend.character.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.character.service.BlockedService;
import sele906.dev.beluo_backend.character.service.LikeService;

@RestController
@RequestMapping("/api/character/blocked")
public class BlockedController {

    @Autowired
    private BlockedService blockedService;

    //차단
    @PostMapping("/{id}")
    public ResponseEntity<Void> addBlocked(@PathVariable String id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        blockedService.addBlocked(auth.getName(), id);
        return ResponseEntity.ok().build();
    }

    //차단 해제
    @DeleteMapping("/{id}")
    public void cancelBlocked(@PathVariable String id, Authentication auth) {
        if (auth != null) {
            blockedService.cancelBlocked(auth.getName(), id);
        }
    }
}
