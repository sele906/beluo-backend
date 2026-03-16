package sele906.dev.beluo_backend.mypage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.mypage.service.MyPageService;
import sele906.dev.beluo_backend.user.domain.User;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    MyPageService myPageService;

    @GetMapping("/info")
    public User info(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.info(userId);
    }
}
