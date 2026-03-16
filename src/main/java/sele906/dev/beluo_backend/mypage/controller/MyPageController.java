package sele906.dev.beluo_backend.mypage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.mypage.service.MyPageService;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.character.domain.Character;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    MyPageService myPageService;

    @GetMapping("/overview")
    public Map<String, Object> overView(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.overView(userId);
    }

    //profile
    @GetMapping("/profile")
    public User profile(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        //구글 로그인 /일반 로그인 여부에 따라 화면 분기처리 해야함

        return myPageService.profile(userId);
    }

    //characters
    @GetMapping("/characters")
    public List<Character> characters(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.characters(userId);
    }

    //liked
    @GetMapping("/liked")
    public List<Character> liked(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.liked(userId);
    }

    //blocked
    @GetMapping("/blocked")
    public List<Character> blocked(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.blocked(userId);
    }
}
