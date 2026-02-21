package sele906.dev.beluo_backend.chat.service;

import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.ai.client.OpenAiClient;
import sele906.dev.beluo_backend.ai.prompt.PromptService;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.ChatRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//채팅 데이터 관리하는 역할

@Service
public class ChatService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private OpenAiClient openAiClient;

    @Autowired
    private PromptService promptService;

    private static final AtomicInteger counter = new AtomicInteger(0);

    //채팅 api 실행
    public String sendChatApi(String userMessage, String chatRoomNum) {

        //10번 테스트 제한
        if (counter.incrementAndGet() > 10) {
            return "오늘 테스트는 여기까지! 🛑";
        }

        //프롬프트 작성
        List<Map<String, String>> prompt = promptService.buildPrompt(chatRoomNum);

        //예외처리
        if (prompt.isEmpty()) {
            throw new IllegalArgumentException("프롬프트 확인 불가");
        }

        //모델 설정

        //api 보내기
        String reply = openAiClient.chat(prompt);

        //예외처리
        if (reply == null) {
            throw new IllegalArgumentException("API 응답 확인 불가");
        }

        return reply;
    }

    //채팅 내용 db에 저장
    public Message chatDataSave(String role, String content, String chatRoomNum) {
        Message m = new Message();
        m.setSessionId(chatRoomNum);
        m.setRole(role);
        m.setContent(content);
        m.setCreatedAt(Instant.now());

        return chatRepository.save(m);
    }

    //요약 후 실행된 대화 카운트
    public void afterSummaryChatCount(String chatRoomNum) {
        //요약 후 대화 횟수 증가
        Query newSummaryQuery = new Query(
                Criteria.where("sessionId").is(chatRoomNum)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        Update summaryUpdate = new Update()
                .inc("sinceLastSummaryCount", 1);

        UpdateResult result = mongoTemplate.updateFirst(newSummaryQuery, summaryUpdate, Message.class);

        //예외처리
        if (result.getMatchedCount() == 0) {
            throw new IllegalStateException("요약 카운트 증가 실패");
        }
    }

    //최근 10개 대화 불러오기
    public List<Message> requestRecentChat(String chatRoomNum) {
        Query query = new Query(
                Criteria.where("sessionId").is(chatRoomNum)
                        .and("role").in("user", "assistant")
        );
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(10);

        List<Message> recentMessages = mongoTemplate.find(query, Message.class);
        Collections.reverse(recentMessages);

        //예외처리
        if (recentMessages.isEmpty()) {
            throw new IllegalArgumentException("최근 대화 목록 확인 불가");
        }

        return recentMessages;
    }

    @PostConstruct
    public void logMongoInfo() {
        //테스트
        System.out.println("Mongo DB = " + mongoTemplate.getDb().getName());
        System.out.println("Collections = " + mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()));
    }

}
