package sele906.dev.beluo_backend.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.user.UserRepository;
import sele906.dev.beluo_backend.character.domain.Blocked;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.BlockedRepository;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.conversation.ConversationRepository;
import sele906.dev.beluo_backend.chat.repository.message.MessageRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlockedRepository blockedRepository;

    //빈 채팅방 생성
    public String createConversation(String characterId, String userId) {

        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new InvalidRequestException("캐릭터를 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("사용자를 찾을 수 없습니다"));

        //conversation 데이터 생성
        Conversation c = new Conversation();
        c.setSessionId(UUID.randomUUID().toString());
        c.setCreatedAt(Instant.now());
        c.setLastChatAt(Instant.now());

        //캐릭터
        c.setCharacterId(String.valueOf(character.getId()));
        c.setCharacterName(character.getCharacterName());
        c.setCharacterImgUrl(character.getCharacterImgUrl());

        //유저
        c.setUserId(user.getId());
        c.setUserEmail(user.getEmail());
        c.setUserName(user.getName());

        //유저 프로필 사진
        c.setUserImgUrl(user.getUserImgUrl());

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

        //시스템 프롬프트 + 캐릭터 프롬프트 (null 필드 제외)
        try {
            ObjectMapper nonNullMapper = objectMapper.copy()
                    .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
            systemMessage.setContent(nonNullMapper.writeValueAsString(character.getPersonalityJson()));
        } catch (Exception e) {
            throw new DataAccessException("캐릭터 설정 직렬화 실패", e);
        }

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

        //첫 메세지 ({{char}}, {{user}} 치환)
        String firstMessage = character.getFirstMessage() != null ? character.getFirstMessage() : "";
        firstMessage = firstMessage
                .replace("{{char}}", character.getCharacterName() != null ? character.getCharacterName() : "")
                .replace("{{user}}", user.getName() != null ? user.getName() : "");
        m.setContent(firstMessage);

        //db에 저장
        try {
            messageRepository.save(m);
        } catch (Exception e) {
            throw new DataAccessException("메세지 저장 실패", e);
        }

        characterRepository.increaseConvCount(c.getCharacterId());

        return c.getSessionId();
    }

    //채팅방 리스트 불러오기
    public List<Conversation> getConversationList(String userId) {

        if (userId == null) {
            return List.of();
        }

        try {
            List<String> blockedIds = blockedRepository.findByUserId(userId).stream()
                    .map(Blocked::getCharacterId)
                    .toList();
            return conversationRepository.findRecentConversations(userId, blockedIds);
        } catch (Exception e) {
            throw new DataAccessException("채팅방 리스트 불러오기 실패", e);
        }
    }

    //빈 채팅방 정보 세팅
    public Map<String, Object> getConversationDetail(String sessionId) {

        Conversation conv = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new InvalidRequestException("대화방을 찾을 수 없습니다"));

        Map<String, Object> map = new HashMap<>();

        map.put("sessionId", sessionId);
        map.put("conversationName", conv.getConversationName());
        map.put("characterName", conv.getCharacterName());
        map.put("characterImgUrl", conv.getCharacterImgUrl());

        map.put("userId", conv.getUserId());
        map.put("userEmail", conv.getUserEmail());
        map.put("userName", conv.getUserName());
        map.put("userImgUrl", conv.getUserImgUrl());

        return map;
    }

    public void editConversationName(String sessionId, String conversationName) {
        try {
            conversationRepository.updateConversationName(sessionId, conversationName);
        } catch (Exception e) {
            throw new DataAccessException("채팅방 이름 변경에 실패했습니다");
        }
    }

    //채팅방 삭제
    public void converstaionDelete(String sessionId, String userId) {
        try {
            conversationRepository.anonymizeOneByUserId(sessionId, userId);
        } catch (Exception e) {
            throw new DataAccessException("채팅방 삭제에 실패했습니다");
        }
    }
}
