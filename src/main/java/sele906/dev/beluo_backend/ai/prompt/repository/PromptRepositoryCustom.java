package sele906.dev.beluo_backend.ai.prompt.repository;

import sele906.dev.beluo_backend.chat.domain.Message;

import java.util.List;

public interface PromptRepositoryCustom {

    // 시스템 프롬프트 불러오기
    Message systemMessage(String sessionId);

    //요약 프롬프트 불러오기
    Message summaryMessage(String sessionId);

    //최근 대화 프롬프트 불러오기
    List<Message> recentMessage(String sessionId, int sinceLastSummaryCount);
}
