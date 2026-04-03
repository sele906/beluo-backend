package sele906.dev.beluo_backend.chat.service;

import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.ai.client.OpenAiClient;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.domain.PersonalityJson;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;
import sele906.dev.beluo_backend.chat.domain.Conversation;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.conversation.ConversationRepository;
import sele906.dev.beluo_backend.chat.repository.message.MessageRepository;
import sele906.dev.beluo_backend.exception.DataAccessException;
import sele906.dev.beluo_backend.exception.SummaryException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//요약 데이터 관리하는 역할

@Service
public class SummaryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private OpenAiClient openAiClient;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Value("${prompt.summary}")
    private String summaryPromptPath;

    private String summaryPromptTemplate;

    @PostConstruct
    public void loadPromptTemplates() throws IOException {
        summaryPromptTemplate = Files.readString(Path.of(summaryPromptPath));
    }

    //요약 채팅 api 실행
    public String summarizeChat(String sessionId) {
        //사용자 몰렸을 때 동시 중복 기능 실행 방지
        //작업 필요
        Update lockUpdate = new Update().set("isSummarizing", true);

        List<Map<String, String>> sendSummaryMessage = new ArrayList<>();

        //요약 데이터 가져오기
        Message summaryMessage = messageRepository.summaryMessage(sessionId);

        //예외처리
        if (summaryMessage == null) {
            throw new DataAccessException("요약 데이터를 확인할 수 없어요. 잠시후 다시 시도해 주세요");
        }

        String content = summaryMessage.getContent();
        Instant lastSummarizedAt = summaryMessage.getLastSummarizedAt();

        // 캐릭터 성격 정보 조회
        Conversation conversationInfo = conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new DataAccessException("대화방 확인 불가"));
        Character character = characterRepository.findById(conversationInfo.getCharacterId())
                .orElseThrow(() -> new DataAccessException("캐릭터 확인 불가"));
        PersonalityJson p = character.getPersonalityJson();

        String personalityType = (p != null && p.getPersonality() != null) ? p.getPersonality().getType() : "알 수 없음";
        String traits = (p != null && p.getPersonality() != null) ? p.getPersonality().getTraits().toString() : "[]";
        int emotionalOpenness = (p != null && p.getPersonality() != null) ? p.getPersonality().getEmotionalOpenness() : 5;
        String tone = (p != null && p.getSpeechStyle() != null) ? p.getSpeechStyle().getTone() : "알 수 없음";

        //요약할 최근 대화
        List<Message> recentMessagesToSummarize = messageRepository.recentMessagesToSummarize(sessionId, lastSummarizedAt);

        //예외처리
        if (recentMessagesToSummarize.isEmpty()) {
            throw new DataAccessException("요약할 최근 대화 목록을 확인할 수 없어요. 잠시후 다시 시도해 주세요");
        }

        String previousSummary = (content != null && !content.isEmpty())
                ? content
                : "없음 (초기 상태, 모든 감정 수치는 캐릭터 성격에 맞게 초기화)";

        //요약 프롬프트 작성
        sendSummaryMessage.add(Map.of(
                "role", "system",
                "content", summaryPromptTemplate.formatted(personalityType, traits, emotionalOpenness, tone, previousSummary)
        ));

        StringBuilder conversation = new StringBuilder();
        for (Message m : recentMessagesToSummarize) {
            String speaker = m.getRole().equals("user") ? "유저" : "캐릭터";
            conversation.append(speaker).append(": ").append(m.getContent()).append("\n");
        }

        sendSummaryMessage.add(Map.of(
                "role", "user",
                "content", "요약할 대화:\n" + conversation
        ));

        //요약 프롬프트 출력
        String finishedSummary = openAiClient.summary(sendSummaryMessage);

        //요약 데이터 업데이트
        //요약 날짜를 10개 최근 대화 중 가장 최근 날짜로 수정
        Instant newLastSummarizedAt =
                recentMessagesToSummarize
                        .get(recentMessagesToSummarize.size() - 1)
                        .getCreatedAt();

        //요약 데이터 업데이트
        UpdateResult result = messageRepository.summaryDataUpdate(sessionId, finishedSummary, newLastSummarizedAt);

        //예외처리
        if (result.getMatchedCount() == 0) {
            throw new SummaryException("요약 대화 데이터를 업데이트할 수 없어요. 잠시후 다시 시도해 주세요");
        }

        return finishedSummary;
    }
}
