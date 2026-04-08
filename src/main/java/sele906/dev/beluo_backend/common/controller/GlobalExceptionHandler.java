package sele906.dev.beluo_backend.common.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sele906.dev.beluo_backend.exception.*;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalid(InvalidRequestException e) {
        return e.getMessage();
    }

    @ExceptionHandler(PromptBuildException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handlePrompt(PromptBuildException e) {
        return e.getMessage();
    }

    @ExceptionHandler(AiResponseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String handleAi(AiResponseException e) {
        String raw = e.getMessage();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(raw);

            // 공통: error.message 필드 사용
            String message = node.path("error").path("message").asText("");

            if (message.isBlank()) {
                message = node.path("message").asText(""); // 일부 provider
            }

            if (message.isBlank()) {
                return "AI 응답에 실패했어요. 다시 시도해주세요.";
            }

            //클로드의 경우 overloaded, rate limit 등 공통 키워드로 분기
            String lowerMsg = message.toLowerCase();
            if (lowerMsg.contains("overloaded") || lowerMsg.contains("capacity")) {
                return "AI 서버가 혼잡해요. 잠시 후 다시 시도해주세요.";
            }
            if (lowerMsg.contains("rate limit") || lowerMsg.contains("too many")) {
                return "요청이 너무 많아요. 잠시 후 다시 시도해주세요.";
            }

            return "AI 응답에 실패했어요. 다시 시도해주세요.";

        } catch (Exception ex) {
            return "AI 응답에 실패했어요. 다시 시도해주세요.";
        }
    }

    @ExceptionHandler(SummaryException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleSummary(SummaryException e) {
        return e.getMessage();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleData(DataAccessException e) {
        log.error(e.getMessage(), e.getCause());
        return e.getMessage();
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIo(IOException e) {
        return "파일 처리 중 오류가 발생했습니다.";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e) {
        log.error("Unhandled exception", e);
        return "서버 오류가 발생했습니다.";
    }
}
