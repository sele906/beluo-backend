package sele906.dev.beluo_backend.exception;

public class SummaryException extends RuntimeException {
    public SummaryException(String message) {
        super(message);
    }

    public SummaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
