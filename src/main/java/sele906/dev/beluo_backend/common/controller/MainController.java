package sele906.dev.beluo_backend.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.chat.service.ChatService;

@RestController
public class MainController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/")
    public ResponseEntity<String> mainPage() {
        return new ResponseEntity<>("hello!", HttpStatus.OK);
    }
}
