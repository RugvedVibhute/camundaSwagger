package dev.rugved.camundaSwagger.exception;

import io.camunda.zeebe.client.api.command.ClientStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle custom validation exceptions
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getErrorCode(), ex.getError(), ex.getErrorDescription()),
                HttpStatus.BAD_REQUEST
        );
    }

    // Handle Spring validation exceptions (e.g., @NotNull, @Valid, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        // Extracting error messages from the validation errors
        String errorDescription = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + " " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new ErrorResponse(400, "Validation Error", errorDescription),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ClientStatusException.class)
    public ResponseEntity<?> handleClientStatusException(ClientStatusException ex) {
        // Extracting the error details from the exception
        String errorMessage = ex.getMessage(); // "Command 'CREATE' rejected with code 'NOT_FOUND'..."

        // Customize the error description for better clarity
        String errorDescription = "BPMN process definition not found: " + errorMessage;

        // Return the custom error response
        ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", errorDescription);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ErrorResponse class to format the JSON response
    public static class ErrorResponse {
        private final int errorCode;
        private final String error;
        private final String errorDescription;

        public ErrorResponse(int errorCode, String error, String errorDescription) {
            this.errorCode = errorCode;
            this.error = error;
            this.errorDescription = errorDescription;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getError() {
            return error;
        }

        public String getErrorDescription() {
            return errorDescription;
        }
    }
}
