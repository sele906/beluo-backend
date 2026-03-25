package sele906.dev.beluo_backend.user.service;

import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.user.domain.ChatCount;
import sele906.dev.beluo_backend.user.repository.chatCount.ChatCountRepository;

import java.time.Instant;

@Service
public class ChatCountService {

    @Autowired
    private ChatCountRepository chatCountRepository;

    public void chatCountSave(String userId) {
        try {
            ChatCount c = new ChatCount();
            c.setUserId(userId);
            c.setChatAt(Instant.now());
            chatCountRepository.save(c);
        } catch (Exception e) {
            throw new DataAccessException("채팅횟수를 카운트 할 수 없습니다");
        }

    }

    public boolean chatLimit(String userId) {
        try {
            return chatCountRepository.countTodayChats(userId) >= 50;
        } catch (Exception e) {
            throw new DataAccessException("채팅횟수 제한에 실패했습니다.");
        }
    }
}
