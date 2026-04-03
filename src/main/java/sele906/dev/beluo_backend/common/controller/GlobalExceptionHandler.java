package sele906.dev.beluo_backend.common.controller;

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
        return e.getMessage();
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
        return "서버 오류가 발생했습니다.";
    }
}
