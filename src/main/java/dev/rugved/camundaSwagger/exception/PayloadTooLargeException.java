package dev.rugved.camundaSwagger.exception;

// 413 - Payload Too Large
public class PayloadTooLargeException extends RuntimeException {
    public PayloadTooLargeException(String message) {
        super(message);
    }
}