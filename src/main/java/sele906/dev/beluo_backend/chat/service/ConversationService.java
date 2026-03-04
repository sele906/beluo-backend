package sele906.dev.beluo_backend.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.conversation.ConversationRepository;
import sele906.dev.beluo_backend.chat.repository.message.MessageRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import java.time.Instant;
import java.util.*;

//채팅방 초기상태 세팅하는 역할

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private MessageRepository messageRepository;

    //빈 채팅방 생성
    public String createConversation(String characterId) {

        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new InvalidRequestException("캐릭터를 찾을 수 없습니다")); //초기세팅

        //conversation 데이터 생성
        Conversation c = new Conversation();
        c.setSessionId(UUID.randomUUID().toString());
        c.setCreatedAt(Instant.now());

        //캐릭터
        c.setCharacterId(String.valueOf(character.getId()));
        c.setCharacterName(character.getCharacterName());
        c.setCharacterThumbFilePath(character.getCharacterThumbFilePath());

        //유저 이름
        c.setUserName("userName"); //초기세팅
        //유저 프로필 사진(나중에 추가)
        //c.setUserProfile("");

        //채팅방 이름
        c.setConversationName(character.getCharacterName() + "와의 새 대화");

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
        systemMessage.setContent(character.getPersonality()); //초기세팅

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
        m.setContent(character.getFirstMessage());

        //db에 저장
        try {
            messageRepository.save(m);
        } catch (Exception e) {
            throw new DataAccessException("메세지 저장 실패", e);
        }

        return c.getSessionId();
    }

    //채팅방 리스트 불러오기
    public List<Conversation> conversationList() {
        List<Conversation> recentConversations;
        try {
            recentConversations = conversationRepository.requestRecentConversations();
        } catch (Exception e) {
            throw new DataAccessException("채팅방 리스트 불러오기 실패", e);
        }

        return recentConversations != null ? recentConversations : List.of();
    }

    public Map<String, Object> getConversationDetail(String sessionId) {

        Conversation conv = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new InvalidRequestException("대화방을 찾을 수 없습니다"));

        List<Message> messages;

        try {
            messages = messageRepository.requestRecentChat(sessionId);
        } catch (Exception e) {
            throw new DataAccessException("대화방 정보 불러오기 실패", e);
        }

        Map<String, Object> map = new HashMap<>();

        map.put("sessionId", sessionId);
        map.put("conversationName", conv.getConversationName());
        map.put("characterName", conv.getCharacterName());
        map.put("characterThumbFilePath", conv.getCharacterThumbFilePath());
        map.put("messages", messages != null ? messages : List.of());

        return map;
    }
}
