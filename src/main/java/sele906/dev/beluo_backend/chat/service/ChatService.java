package sele906.dev.beluo_backend.chat.service;

import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.ai.client.ClaudeClient;
import sele906.dev.beluo_backend.ai.client.OpenAiClient;
import sele906.dev.beluo_backend.ai.client.OpenRouterClient;
import sele906.dev.beluo_backend.ai.prompt.dto.PromptData;
import sele906.dev.beluo_backend.ai.prompt.service.PromptService;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.conversation.ConversationRepository;
import sele906.dev.beluo_backend.chat.repository.message.MessageRepository;
import sele906.dev.beluo_backend.exception.AiResponseException;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.PromptBuildException;
import sele906.dev.beluo_backend.exception.SummaryException;
import sele906.dev.beluo_backend.user.domain.User;
import sele906.dev.beluo_backend.user.repository.user.UserRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//채팅 데이터 관리하는 역할

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private OpenAiClient openAiClient;

    @Autowired
    private OpenRouterClient openRouterClient;

    @Autowired
    private ClaudeClient claudeClient;

    @Autowired
    private PromptService promptService;

    @Autowired
    private UserRepository userRepository;

    //채팅 api 실행
    public String sendChatApi(String sessionId, String userId) {

        //프롬프트 작성
        PromptData promptData = promptService.buildPrompt(sessionId);

        //예외처리
        if (promptData.getSystemMessages().isEmpty() && promptData.getRecentMessages().isEmpty()) {
            throw new PromptBuildException("프롬프트를 확인할 수 없어요");
        }

        //api 보내기
        //유저별 분기처리
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new DataAccessException("유저를 확인할 수 없어요"));

        String reply = null;

        if (u.getAiModel().equals("free")) {
            reply = openRouterClient.freeChat(promptData);
        } else if (u.getAiModel().equals("gpt")) {
            reply = openAiClient.chat(promptData);
        } else if (u.getAiModel().equals("claude")) {
            reply = claudeClient.chat(promptData);
        }

        //예외처리
        if (reply == null) {
            throw new AiResponseException("API 응답을 확인할 수 없어요");
        }

        return reply;
    }

    //채팅 내용 db에 저장
    public Message chatDataSave(String role, String content, String sessionId) {
        try {
            Message m = new Message();
            m.setSessionId(sessionId);
            m.setRole(role);
            m.setContent(content);
            m.setCreatedAt(Instant.now());

            //대화 db에 최신 대화 시간 저장
            Conversation conversation = conversationRepository.findBySessionId(sessionId).orElseThrow();
            conversation.setLastChatAt(m.getCreatedAt());
            conversationRepository.save(conversation);

            return messageRepository.save(m);
        } catch (Exception e) {
            throw new DataAccessException("메세지 저장에 실패했습니다", e);
        }
    }

    //메세지 삭제 (AI 호출 실패 시 롤백용)
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }

    //고아 메세지 삭제 (유저가 응답 대기 중 이탈 시 롤백용)
    public void deleteOrphanUserMessage(String sessionId) {
        Message last = messageRepository.findLastBySessionId(sessionId);
        if (last != null && "user".equals(last.getRole())) {
            messageRepository.deleteById(last.getId());
        }
    }

    //요약 후 실행된 대화 카운트
    public void afterSummaryChatCount(String sessionId) {

        //요약 후 대화 횟수 증가
        UpdateResult result = messageRepository.afterSummaryChatCount(sessionId);

        //예외처리
        if (result.getMatchedCount() == 0) {
            throw new SummaryException("요약 카운트 증가에 실패했습니다");
        }
    }

    //무한 스크롤 대화 불러오기
    public Map<String, Object> requestRecentChat(String sessionId, String before) {

        Instant cursor = (before != null && !before.isBlank())
                ? Instant.parse(before)
                : Instant.now(); // before 없으면 현재 시각 기준

        int limit = 10;
        List<Message> messages;
        try {
            messages = messageRepository.requestChatBefore(sessionId, cursor, limit);
        } catch (Exception e) {
            throw new DataAccessException("메세지를 불러올 수 없어요", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("hasMore", messages.size() == limit); // 10개면 더 있을 수 있음

        // 다음 커서 - 가장 오래된 메시지의 createdAt
        if (!messages.isEmpty()) {
            result.put("nextCursor", messages.get(0).getCreatedAt().toString());
        }

        return result;
    }

    public void chatEdit(String sessionId, String messageId, String content) {
        try {
            messageRepository.updateMessage(sessionId, messageId, content);
        } catch (Exception e) {
            throw new DataAccessException("메세지 수정에 실패했습니다.");
        }
    }
}
