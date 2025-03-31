package dev.rugved.camundaSwagger.exception;

// 409 - Conflict
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
