package sele906.dev.beluo_backend.exception;

public class PromptBuildException extends RuntimeException {
    public PromptBuildException(String message) {
        super(message);
    }

    public PromptBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
