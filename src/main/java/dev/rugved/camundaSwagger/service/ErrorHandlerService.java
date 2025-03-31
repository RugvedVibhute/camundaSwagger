package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.util.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ErrorHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerService.class);

    public Map<String, Object> handleError(Exception e, String jobType) {
        logger.error("Error processing {} job: {}", jobType, e.getMessage(), e);

        Map<String, Object> errorOutput = new HashMap<>();
        ErrorCodes errorCode;

        // Determine appropriate error code based on exception type
        if (e instanceof IllegalArgumentException) {
            errorCode = ErrorCodes.INVALID_INPUT;
        } else if (e.getMessage() != null && e.getMessage().contains("No data found")) {
            errorCode = ErrorCodes.DATA_NOT_FOUND;
        } else if (e instanceof jakarta.persistence.PersistenceException) {
            errorCode = ErrorCodes.DATABASE_ERROR;
        } else {
            errorCode = ErrorCodes.UNEXPECTED_ERROR;
        }

        // Add error details to output
        errorOutput.put("errorCode", errorCode.getCode());
        errorOutput.put("errorMessage", errorCode.getDefaultMessage() + ": " + e.getMessage());

        return errorOutput;
    }
}