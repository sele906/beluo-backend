package sele906.dev.beluo_backend.mypage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.exception.InvalidRequestException;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.UserRepository;

@Service
public class MyPageService {

    @Autowired
    UserRepository userRepository;

    public User info(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("회원정보 불러오기 실패"));

        //내가 만든 캐릭터

        //내가 관심있는 캐릭터

        //내가 차단한 캐릭터

        return user;
    }
}
