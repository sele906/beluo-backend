package sele906.dev.beluo_backend.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sele906.dev.beluo_backend.exception.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
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
        return e.getMessage();
    }
}
