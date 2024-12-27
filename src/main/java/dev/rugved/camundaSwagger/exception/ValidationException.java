package dev.rugved.camundaSwagger.exception;

public class ValidationException extends RuntimeException {
    private final int errorCode;
    private final String error;
    private final String errorDescription;

    public ValidationException(int errorCode, String error, String errorDescription) {
        super(error);
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

