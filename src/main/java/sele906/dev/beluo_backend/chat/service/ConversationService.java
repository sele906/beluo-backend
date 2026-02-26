package sele906.dev.beluo_backend.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.conversation.ConversationRepository;
import sele906.dev.beluo_backend.chat.repository.message.MessageRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

//채팅방 초기상태 세팅하는 역할

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    //빈 채팅방 생성
    public String createConversation() {

        //conversation 데이터 생성
        Conversation c = new Conversation();
        c.setSessionId(UUID.randomUUID().toString());
        c.setCreatedAt(Instant.now());

        //캐릭터 이름
        c.setCharacterName("connor"); //초기세팅
        //캐릭터 프로필 사진(나중에 추가)
        //c.setCharacterProfile("");

        //유저 이름
        c.setUserName("userName"); //초기세팅
        //유저 프로필 사진(나중에 추가)
        //c.setUserProfile("");

        //채팅방 이름
        c.setConversationName(c.getCharacterName() + "와의 새 대화");

        //db에 저장
        try {
            conversationRepository.save(c);
        } catch (Exception e) {
            throw new DataAccessException("대화 세팅 저장 실패", e);
        }

        //시스템 프롬프트 생성
        Message systemMessage = new Message();
        systemMessage.setSessionId(c.getSessionId());
        systemMessage.setRole("system");
        systemMessage.setCreatedAt(Instant.now());
        systemMessage.setType("system");

        //시스템 프롬프트 + 캐릭터 프롬프트
        systemMessage.setContent("코너는 디트로이트 비컴 휴먼의 친절한 안드로이드이다"); //초기세팅

        //db 저장
        try {
            messageRepository.save(systemMessage);
        } catch (Exception e) {
            throw new DataAccessException("시스템 메세지 저장 실패", e);
        }

        //빈 요약 프롬프트 생성
        Message summaryMessage = new Message();
        summaryMessage.setSessionId(c.getSessionId());
        summaryMessage.setRole("system");
        summaryMessage.setCreatedAt(Instant.now());
        summaryMessage.setType("summary");
        summaryMessage.setContent("");
        summaryMessage.setLastSummarizedAt(Instant.now());
        summaryMessage.setSummaryVersion(0);
        summaryMessage.setSinceLastSummaryCount(1);
        summaryMessage.setIsSummarizing(false);

        //db 저장
        try {
            messageRepository.save(summaryMessage);
        } catch (Exception e) {
            throw new DataAccessException("요약 메세지 저장 실패", e);
        }

        //첫 메세지 생성
        Message m = new Message();
        m.setSessionId(c.getSessionId());
        m.setRole("assistant");
        m.setCreatedAt(Instant.now());

        //첫 메세지
        m.setContent("안녕하세요, 저는 사이버라이프에서 만들어진 안드로이드 코너입니다"); //초기세팅

        try {
            messageRepository.save(m);
        } catch (Exception e) {
            throw new DataAccessException("메세지 저장 실패", e);
        }

        //db에 저장

        return "success";
    }

    //채팅방 리스트 불러오기
    public List<Conversation> conversationList() {
        List<Conversation> recentConversations = conversationRepository.requestRecentConversations();

        //예외처리
        if (recentConversations.isEmpty()) {
            throw new DataAccessException("최근 채팅방 목록 확인 불가");
        }

        return recentConversations;
    }
}
