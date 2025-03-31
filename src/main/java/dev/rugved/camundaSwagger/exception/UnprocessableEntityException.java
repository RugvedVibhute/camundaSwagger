package dev.rugved.camundaSwagger.exception;

// 422 - Unprocessable Entity
public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
