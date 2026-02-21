package sele906.dev.beluo_backend.ai.prompt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.service.SummaryService;
import java.util.*;

//프롬프트 조립하는 역할

@Service
public class PromptService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SummaryService summaryService;

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
                    "content", systemPrompt.getContent()
            ));
        }

        //요약 프롬프트 > 최종 프롬프트에 결합
        if (summaryPrompt != null) {
            promptMessage.add(Map.of(
                    "role", "system",
                    "content", """PROMPT_REMOVED""" + summaryPrompt
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
        Query systemQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("system")
        );

        Message systemMessage = mongoTemplate.findOne(systemQuery, Message.class);

        //예외처리
        if (systemMessage == null) {
            throw new IllegalStateException("시스템 데이터 확인 불가");
        }

        return systemMessage;
    }

    private String buildSummaryPrompt(String sessionId) {

        //요약 프롬프트 불러오기
        Query summaryQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        Message summaryMessage = mongoTemplate.findOne(summaryQuery, Message.class);

        //예외처리
        if (summaryMessage == null) {
            throw new IllegalArgumentException("요약 데이터 확인 불가");
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
                throw new IllegalArgumentException("요약 응답 확인 불가");
            } else {
                return null;
            }

        } else {
            return content;
        }
    }

    private List<Message> buildRecentMessagePrompt(String sessionId){

        //요약 이후 쌓인 대화 수
        Query summaryQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").is("system")
                        .and("type").is("summary")
        );

        Message summaryMessage = mongoTemplate.findOne(summaryQuery, Message.class);

        //예외처리
        if (summaryMessage == null) {
            throw new IllegalArgumentException("요약 데이터 확인 불가");
        }

        int sinceLastSummaryCount = summaryMessage.getSinceLastSummaryCount();

        //최근 대화 불러오기
        Query chatQuery = new Query(
                Criteria.where("sessionId").is(sessionId)
                        .and("role").in("user", "assistant")
        );

        chatQuery.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        chatQuery.limit(sinceLastSummaryCount);

        List<Message> recentMessages = mongoTemplate.find(chatQuery, Message.class);
        Collections.reverse(recentMessages);

        //예외처리
        if (recentMessages.isEmpty()) {
            throw new IllegalArgumentException("최근 대화 데이터 확인 불가");
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
}
