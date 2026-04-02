package sele906.dev.beluo_backend.ai.prompt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.ai.prompt.dto.PromptData;
import sele906.dev.beluo_backend.ai.prompt.repository.PromptRepository;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.conversation.ConversationRepository;
import sele906.dev.beluo_backend.chat.service.SummaryService;
import sele906.dev.beluo_backend.exception.PromptBuildException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

//프롬프트 조립하는 역할

@Service
public class PromptService {

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationRepository conversationRepository;

    @Value("classpath:static/system_prompt.txt")
    private Resource systemPromptResource;

    @Value("classpath:static/summary_short_prompt.txt")
    private Resource summaryPromptResource;

    private String systemPromptTemplate;
    private String summaryPromptTemplate;

    @PostConstruct
    public void loadPromptTemplates() throws IOException {
        systemPromptTemplate = new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        summaryPromptTemplate = new String(summaryPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    //최종 프롬프트
    public PromptData buildPrompt(String sessionId) {

        List<Map<String, String>> systemMessages = new ArrayList<>();
        List<Map<String, String>> recentMessages = new ArrayList<>();

        //시스템 프롬프트 함수
        Message systemPrompt = buildSystemPrompt(sessionId);

        //요약 프롬프트 함수
        String summaryPrompt = buildSummaryPrompt(sessionId);

        //최근 대화 프롬프트 함수
        List<Message> recentMessagePrompt = buildRecentMessagePrompt(sessionId);

        //시스템 프롬프트 결합
        if (systemPrompt != null) {
            systemMessages.add(Map.of(
                    "role", systemPrompt.getRole(),
                    "content", systemPromptTemplate + systemPrompt.getContent()
            ));
        }

        //요약 프롬프트 결합
        if (summaryPrompt != null && !summaryPrompt.isEmpty()) {
            systemMessages.add(Map.of(
                    "role", "system",
                    "content", buildSummaryInstruction(summaryPrompt)
            ));
        }

        //최근 대화 결합
        for (Message m : recentMessagePrompt) {
            recentMessages.add(Map.of(
                    "role", m.getRole(),
                    "content", m.getContent()
            ));
        }

        return new PromptData(systemMessages, recentMessages);
    }


    private Message buildSystemPrompt(String sessionId) {

        // 시스템 프롬프트 불러오기
        Message systemMessage = promptRepository.systemMessage(sessionId);

        //예외처리
        if (systemMessage == null) {
            throw new PromptBuildException("시스템 프롬프트를 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        // {{char}}, {{user}} 변수 치환
        Conversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new PromptBuildException("대화방을 확인할 수 없어요. 잠시 후 다시 시도해 주세요"));

        String content = systemMessage.getContent()
                .replace("{{char}}", conversation.getCharacterName() != null ? conversation.getCharacterName() : "")
                .replace("{{user}}", conversation.getUserName() != null ? conversation.getUserName() : "");

        systemMessage.setContent(content);
        return systemMessage;
    }

    private String buildSummaryPrompt(String sessionId) {

        //요약 프롬프트 불러오기
        Message summaryMessage = promptRepository.summaryMessage(sessionId);

        //예외처리
        if (summaryMessage == null) {
            throw new PromptBuildException("요약 프롬프트를 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        String content = summaryMessage.getContent();
        int sinceLastSummaryCount = summaryMessage.getSinceLastSummaryCount();

        //요약 이후 대화가 10개 쌓였으면 대화 요약 실행
        if (sinceLastSummaryCount > 10 && !summaryMessage.getIsSummarizing()) {

            try {
                String finishedSummary = summaryService.summarizeChat(sessionId);
                if (finishedSummary != null) {
                    return finishedSummary;
                }
            } catch (Exception e) {
                // 요약 실패 시 기존 요약으로 폴백
            }

            return content; // 요약 실패 시 기존 요약으로 폴백

        } else {
            return content;
        }
    }

    private List<Message> buildRecentMessagePrompt(String sessionId){

        Message summaryMessage = promptRepository.summaryMessage(sessionId);

        //예외처리
        if (summaryMessage == null) {
            throw new PromptBuildException("요약 데이터를 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        int sinceLastSummaryCount = summaryMessage.getSinceLastSummaryCount();

        //최근 대화 프롬프트 불러오기
        List<Message> recentMessages = promptRepository.recentMessage(sessionId, sinceLastSummaryCount);

        //예외처리
        if (recentMessages.isEmpty()) {
            throw new PromptBuildException("최근 대화 데이터를 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        return recentMessages;
    }

    private String buildSummaryInstruction(String summaryJson) {
        try {
            Map<String, Object> summary = objectMapper.readValue(summaryJson, Map.class);

            Map<String, Object> emotionalState = (Map<String, Object>) summary.get("emotionalState");
            String conversationSummary = (String) summary.get("conversationSummary");
            String relationshipPhase = (String) summary.get("relationshipPhase");

            if (emotionalState == null) {
                return "다음은 이전 대화 요약이다. 반드시 반영하여 답변하라.\n\n" + summaryJson;
            }

            int affection = (int) emotionalState.getOrDefault("affection", 0);
            int trust = (int) emotionalState.getOrDefault("trust", 0);
            int hostility = (int) emotionalState.getOrDefault("hostility", 0);
            int comfort = (int) emotionalState.getOrDefault("comfort", 0);

            return summaryPromptTemplate.formatted(affection, trust, hostility, comfort, relationshipPhase, conversationSummary);
        } catch (Exception e) {
            // JSON 파싱 실패 시 텍스트 그대로 사용 (이전 포맷 호환)
            return "다음은 이전 대화에서 형성된 관계와 감정에 대한 요약이다. 반드시 이 내용을 반영하여 캐릭터의 말투, 태도, 감정이 자연스럽게 이어지도록 답변하라.\n\n" + summaryJson;
        }
    }
}
