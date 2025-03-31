package dev.rugved.camundaSwagger.exception;

// 429 - Too Many Requests
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
