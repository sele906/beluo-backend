package sele906.dev.beluo_backend.ai.prompt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.ai.prompt.repository.PromptRepository;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.SummaryService;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.PromptBuildException;
import sele906.dev.beluo_backend.exception.SummaryException;

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

    //최종 프롬프트
    public List<Map<String, String>> buildPrompt(String sessionId) {

        List<Map<String, String>> promptMessage = new ArrayList<>();

        //시스템 프롬프트 함수
        Message systemPrompt = buildSystemPrompt(sessionId);

        //요약 프롬프트 함수
        String summaryPrompt = buildSummaryPrompt(sessionId);

        //최근 대화 프롬프트 함수
        List<Message> recentMessagePrompt = buildRecentMessagePrompt(sessionId);

        //최종 결합

        //시스템 프롬프트 > 최종 프롬프트에 결합
        if (systemPrompt != null) {
            promptMessage.add(Map.of(
                    "role", systemPrompt.getRole(),
                    "content", """PROMPT_REMOVED""" + systemPrompt.getContent() //캐릭터 프롬프트
            ));
        }

        //요약 프롬프트 > 최종 프롬프트에 결합
        if (summaryPrompt != null && !summaryPrompt.isEmpty()) {
            System.out.println("summaryPrompt: " + summaryPrompt);
            promptMessage.add(Map.of(
                    "role", "system",
                    "content", buildSummaryInstruction(summaryPrompt)
            ));
        }

        //최근 대화 프롬프트 > 최종 프롬프트에 결합
        if (!recentMessagePrompt.isEmpty()) {
            for (Message m : recentMessagePrompt) {
                promptMessage.add(Map.of(
                        "role", m.getRole(),
                        "content", m.getContent()
                ));
            }
        }

        //테스트용
        //최종 프롬프트 출력
        System.out.println("=========완성된 최종 프롬프트=========");

        for (Map p : promptMessage) {
            System.out.println(p.toString());
        }

        System.out.println("==================");

        return promptMessage;
    }


    private Message buildSystemPrompt(String sessionId) {

        // 시스템 프롬프트 불러오기
        Message systemMessage = promptRepository.systemMessage(sessionId);

        //예외처리
        if (systemMessage == null) {
            throw new PromptBuildException("시스템 프롬프트 확인 불가");
        }

        return systemMessage;
    }

    private String buildSummaryPrompt(String sessionId) {

        //요약 프롬프트 불러오기
        Message summaryMessage = promptRepository.summaryMessage(sessionId);

        //예외처리
        if (summaryMessage == null) {
            throw new PromptBuildException("요약 프롬프트 확인 불가");
        }

        String content = summaryMessage.getContent();
        int sinceLastSummaryCount = summaryMessage.getSinceLastSummaryCount();

        //테스트
        System.out.println("=========요약 프롬프트 불러오기=========");

        System.out.println("content:  " + content);
        System.out.println("sinceLastSummaryCount: " + sinceLastSummaryCount);

        System.out.println("==================");

        //요약 이후 대화가 10개 쌓였으면 대화 요약 실행
        if (sinceLastSummaryCount > 10 && !summaryMessage.getIsSummarizing()) {

            String finishedSummary = summaryService.summarizeChat(sessionId);

            //테스트
            System.out.println("=========완성된 새로운 요약 프롬프트=========");

            System.out.println("finishedSummary: " + finishedSummary);

            System.out.println("==================");

            //예외처리
            if (finishedSummary == null) {
                throw new PromptBuildException("요약 응답 확인 불가");
            } else {
                return finishedSummary;
            }

        } else {
            return content;
        }
    }

    private List<Message> buildRecentMessagePrompt(String sessionId){

        Message summaryMessage = promptRepository.summaryMessage(sessionId);

        //예외처리
        if (summaryMessage == null) {
            throw new PromptBuildException("요약 데이터 확인 불가");
        }

        int sinceLastSummaryCount = summaryMessage.getSinceLastSummaryCount();

        //최근 대화 프롬프트 불러오기
        List<Message> recentMessages = promptRepository.recentMessage(sessionId, sinceLastSummaryCount);

        //예외처리
        if (recentMessages.isEmpty()) {
            throw new PromptBuildException("최근 대화 데이터 확인 불가");
        }

        //테스트용
        //최종 프롬프트에 첨부될 최근 대화 출력
        System.out.println("=========최종 프롬프트에 첨부될 최근 대화=========");

        System.out.println("sinceLastSummaryCount: " + sinceLastSummaryCount);

        for (Message r : recentMessages) {
            System.out.println(r.getContent());
        }

        System.out.println("==================");

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

            return """PROMPT_REMOVED""".formatted(affection, trust, hostility, comfort, relationshipPhase, conversationSummary);
        } catch (Exception e) {
            // JSON 파싱 실패 시 텍스트 그대로 사용 (이전 포맷 호환)
            return "다음은 이전 대화에서 형성된 관계와 감정에 대한 요약이다. 반드시 이 내용을 반영하여 캐릭터의 말투, 태도, 감정이 자연스럽게 이어지도록 답변하라.\n\n" + summaryJson;
        }
    }
}
