package sele906.dev.beluo_backend.chat.repository.message;

import com.mongodb.client.result.UpdateResult;
import sele906.dev.beluo_backend.chat.domain.Message;

import java.time.Instant;
import java.util.List;

public interface MessageRepositoryCustom {

    //Chat

    //요약 후 대화 횟수 증가
    UpdateResult afterSummaryChatCount(String sessionId);

    //최근 10개 대화 불러오기
    List<Message> requestChatBefore(String sessionId, Instant before, int limit);

    //Summary

    //요약 데이터 가져오기
    Message summaryMessage(String sessionId);

    //요약할 최근 대화
    List<Message> recentMessagesToSummarize(String sessionId, Instant lastSummarizedAt);

    //요약할 데이터 업데이트
    UpdateResult summaryDataUpdate(String sessionId, String finishedSummary, Instant newLastSummarizedAt);

    void updateMessage(String sessionId, String messageId, String content);
}
