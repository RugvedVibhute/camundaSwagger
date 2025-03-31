package dev.rugved.camundaSwagger.util;

public enum ErrorCodes {
    DATA_NOT_FOUND("ERR-1001", "Required data not found"),
    INVALID_INPUT("ERR-1002", "Invalid input data"),
    DATABASE_ERROR("ERR-1003", "Database operation failed"),
    UNEXPECTED_ERROR("ERR-1004", "Unexpected error occurred");

    private final String code;
    private final String defaultMessage;

    ErrorCodes(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}