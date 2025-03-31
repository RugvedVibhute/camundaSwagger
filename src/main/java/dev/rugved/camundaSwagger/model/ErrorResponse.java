package dev.rugved.camundaSwagger.model;

public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String errorDetails;

    // Standard constructors, getters, setters

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public ErrorResponse(String errorCode, String errorMessage, String errorDetails) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
    }

    public static ErrorResponse of(String errorCode, String errorMessage) {
        return new ErrorResponse(errorCode, errorMessage, null);
    }

    public static ErrorResponse of(String errorCode, String errorMessage, String errorDetails) {
        return new ErrorResponse(errorCode, errorMessage, errorDetails);
    }
}