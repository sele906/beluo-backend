package sele906.dev.beluo_backend.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/refresh")
    public String refresh() {
        return null;
    }

    @PostMapping("/logout")
    public void logout() {

    }
}
