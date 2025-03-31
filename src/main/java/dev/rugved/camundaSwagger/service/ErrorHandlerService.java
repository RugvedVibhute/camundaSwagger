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
        String errorDetails = null;

        // Determine appropriate error code based on exception type and message
        if (e instanceof IllegalArgumentException) {
            String message = e.getMessage() != null ? e.getMessage() : "Invalid input";

            if (message.contains("No Vendor Type found")) {
                errorCode = ErrorCodes.DATA_NOT_FOUND;
                errorDetails = "Missing vendor type information";
            } else if (message.contains("No aaUniSfp found") ||
                    message.contains("No NTU Type found") ||
                    message.contains("No NTU NNI SFP found") ||
                    message.contains("No AA SFP found") ||
                    message.contains("No AA UNI SFP found")) {
                errorCode = ErrorCodes.DATA_NOT_FOUND;
                errorDetails = "Required component not found in database";
            } else if (message.contains("No SKU ID found") ||
                    message.contains("No skuId found")) {
                errorCode = ErrorCodes.DATA_NOT_FOUND;
                errorDetails = "SKU ID not found for specified component";
            } else {
                errorCode = ErrorCodes.INVALID_INPUT;
                errorDetails = message;
            }
        } else if (e.getMessage() != null && e.getMessage().contains("No data found")) {
            errorCode = ErrorCodes.DATA_NOT_FOUND;
        } else if (e instanceof jakarta.persistence.PersistenceException) {
            errorCode = ErrorCodes.DATABASE_ERROR;
            errorDetails = "Database operation failed: " + e.getMessage();
        } else {
            errorCode = ErrorCodes.UNEXPECTED_ERROR;
            errorDetails = e.getMessage();
        }

        // Add error details to output
        errorOutput.put("errorCode", errorCode.getCode());
        errorOutput.put("errorMessage", errorCode.getDefaultMessage() +
                (errorDetails != null ? ": " + errorDetails : ": " + e.getMessage()));
        errorOutput.put("errorDetails", errorDetails);

        return errorOutput;
    }
}