package sele906.dev.beluo_backend.mypage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.mypage.service.MyPageService;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.character.domain.Character;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    MyPageService myPageService;

    @GetMapping("/overview")
    public Map<String, Object> overview(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.overview(userId);
    }

    //profile
    @GetMapping("/profile")
    public User profile(Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        return myPageService.profile(userId);
    }

    //프로필 수정
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void profileEdit(
            @RequestPart("user") User user,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication auth) throws IOException {
        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        myPageService.profileEdit(userId, user, file);
    }

    //회원탈퇴
    @DeleteMapping("/profile")
    public void profileDelete(Authentication auth, HttpServletResponse response) {
        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        myPageService.profileDelete(userId);

        // 쿠키 즉시 만료 (로그아웃과 동일)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(0).build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
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

    //캐릭터 상세 페이지
    @GetMapping("/characters/{id}")
    public Character characterDetail(@PathVariable String id, Authentication auth) {

        if (auth == null) {
            throw new InvalidRequestException("로그인이 필요합니다");
        }

        return myPageService.characterDetail(id);
    }

    //캐릭터 삭제
    @DeleteMapping("/characters/{id}")
    public void characterDelete(@PathVariable String id, Authentication auth) {
        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        myPageService.characterDelete(id, userId);
    }

    //캐릭터 수정
    @PatchMapping(value = "/characters/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void characterEdit(
            @PathVariable String id,
            @RequestPart("character") Character character,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication auth) throws IOException {
        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        myPageService.characterEdit(id, character, file, userId);
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

    //문의사항
    @PostMapping("/inquiry")
    public void submitInquiry(@RequestBody Map<String, String> body, Authentication auth) {

        String userId = null;

        if (auth != null) {
            userId = auth.getName();
        }

        myPageService.submitInquiry(userId, body.get("content"));
    }

}
