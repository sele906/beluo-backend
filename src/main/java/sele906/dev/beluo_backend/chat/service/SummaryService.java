package sele906.dev.beluo_backend.chat.service;

import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.ai.client.OpenAiClient;
import sele906.dev.beluo_backend.chat.domain.Message;
import sele906.dev.beluo_backend.chat.repository.message.MessageRepository;

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
            throw new IllegalArgumentException("요약 데이터 확인 불가");
        }

        String content = summaryMessage.getContent();
        Instant lastSummarizedAt = summaryMessage.getLastSummarizedAt();

        //요약할 최근 대화
        List<Message> recentMessagesToSummarize = messageRepository.recentMessagesToSummarize(sessionId, lastSummarizedAt);

        //예외처리
        if (recentMessagesToSummarize.isEmpty()) {
            throw new IllegalArgumentException("요약할 최근 대화 목록 확인 불가");
        }

        //테스트용
        //요약 프롬프트에 첨부될 최근 대화 출력
        System.out.println("=========요약 프롬프트에 첨부될 최근 대화=========");

        for (Message r : recentMessagesToSummarize) {
            System.out.println(r.getContent());
        }

        System.out.println("==================");

        //요약 프롬프트 작성
        sendSummaryMessage.add(Map.of(
                "role", "system",
                "content", """
                                    이전에 요약된 내용과 함께 대화 내용을 요약해라

                                    """ + content
        ));

        for (Message m : recentMessagesToSummarize) {
            sendSummaryMessage.add(Map.of(
                    "role", m.getRole(),
                    "content", m.getContent()
            ));
        }

        //요약 프롬프트 출력
        String finishedSummary = openAiClient.chat(sendSummaryMessage);

        //요약 데이터 업데이트
        //요약 날짜를 10개 최근 대화 중 가장 최근 날짜로 수정
        Instant newLastSummarizedAt =
                recentMessagesToSummarize
                        .get(recentMessagesToSummarize.size() - 1)
                        .getCreatedAt();

        //테스트
        System.out.println("=========요약 데이터 날짜 확인=========");

        System.out.println("newLastSummarizedAt" + newLastSummarizedAt);

        System.out.println("==================");

        //요약 데이터 업데이트
        UpdateResult result = messageRepository.summaryDataUpdate(sessionId, finishedSummary, newLastSummarizedAt);

        //예외처리
        if (result.getMatchedCount() == 0) {
            throw new RuntimeException("요약 대화 데이터 업데이트 실패");
        }

        return finishedSummary;
    }
}
