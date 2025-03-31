package dev.rugved.camundaSwagger.exception;

// 503 - Service Unavailable
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
