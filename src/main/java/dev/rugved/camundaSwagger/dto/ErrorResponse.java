package dev.rugved.camundaSwagger.dto;

public class ErrorResponse {
    private String errorCode;
    private String error;
    private String errorDescription;

    // Constructor for validation errors
    public ErrorResponse(String errorCode, String error, String errorDescription) {
        this.errorCode = errorCode;
        this.error = error;
        this.errorDescription = errorDescription;
    }

    // Constructor for server errors
    public ErrorResponse(String errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
    }

    // Getters and setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}