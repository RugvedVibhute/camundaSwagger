package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.util.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling and categorizing errors in a consistent way across the application.
 * This improved implementation maintains compatibility with existing code while
 * providing better organization and maintainability.
 */
@Service
public class ErrorHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerService.class);

    /**
     * Handles an exception and creates a standardized error output map.
     *
     * @param e The exception to handle
     * @param jobType The type of job where the error occurred
     * @return Map containing standardized error information
     */
    public Map<String, Object> handleError(Exception e, String jobType) {
        logger.error("Error processing {} job: {}", jobType, e.getMessage(), e);

        // Categorize the error
        ErrorCodes errorCode;
        String errorDetails = null;

        if (e instanceof IllegalArgumentException) {
            // Handle IllegalArgumentException with more detailed categorization
            errorCode = categorizeIllegalArgumentException(e, errorDetails);
            errorDetails = extractErrorDetails(e);
        } else if (e instanceof jakarta.persistence.PersistenceException) {
            // Handle database-related exceptions
            errorCode = ErrorCodes.DATABASE_ERROR;
            errorDetails = "Database operation failed: " + e.getMessage();
        } else {
            // Default fallback categorization
            errorCode = categorizeGenericException(e);
            errorDetails = e.getMessage();
        }

        // Build the standardized error output
        return buildErrorResponse(errorCode, errorDetails);
    }

    /**
     * Categorizes IllegalArgumentException into specific error types.
     */
    private ErrorCodes categorizeIllegalArgumentException(Exception e, String errorDetails) {
        String message = e.getMessage() != null ? e.getMessage() : "Invalid input";

        // Check for data not found patterns
        if (message.contains("No Vendor Type found") ||
                message.contains("No aaUniSfp found") ||
                message.contains("No NTU Type found") ||
                message.contains("No NTU NNI SFP found") ||
                message.contains("No AA SFP found") ||
                message.contains("No AA UNI SFP found") ||
                message.contains("No SKU ID found") ||
                message.contains("No skuId found") ||
                message.contains("No data found")) {
            return ErrorCodes.DATA_NOT_FOUND;
        }

        // Otherwise, it's likely an invalid input error
        return ErrorCodes.INVALID_INPUT;
    }

    /**
     * Categorizes generic exceptions based on their message patterns.
     */
    private ErrorCodes categorizeGenericException(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "";

        if (message.contains("No data found") ||
                message.contains("not found") ||
                message.contains("does not exist")) {
            return ErrorCodes.DATA_NOT_FOUND;
        } else if (message.contains("Invalid") ||
                message.contains("invalid") ||
                message.contains("Missing") ||
                message.contains("missing")) {
            return ErrorCodes.INVALID_INPUT;
        } else if (message.contains("database") ||
                message.contains("SQL") ||
                message.contains("connection")) {
            return ErrorCodes.DATABASE_ERROR;
        } else {
            return ErrorCodes.UNEXPECTED_ERROR;
        }
    }

    /**
     * Extracts detailed error information from an exception.
     */
    private String extractErrorDetails(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Unknown error";

        // Extract more specific details for data not found errors
        if (message.contains("No Vendor Type found")) {
            return "Missing vendor type information";
        } else if (message.contains("No aaUniSfp found") ||
                message.contains("No NTU Type found") ||
                message.contains("No NTU NNI SFP found") ||
                message.contains("No AA SFP found") ||
                message.contains("No AA UNI SFP found")) {
            return "Required component not found in database";
        } else if (message.contains("No SKU ID found") ||
                message.contains("No skuId found")) {
            return "SKU ID not found for specified component";
        }

        // Default to original message for other cases
        return message;
    }

    /**
     * Builds a standardized error response map.
     */
    private Map<String, Object> buildErrorResponse(ErrorCodes errorCode, String errorDetails) {
        Map<String, Object> errorOutput = new HashMap<>();

        // Add error details to output
        errorOutput.put("errorCode", errorCode.getCode());
        errorOutput.put("errorMessage", errorCode.getDefaultMessage() +
                (errorDetails != null ? ": " + errorDetails : ""));
        errorOutput.put("errorDetails", errorDetails);

        return errorOutput;
    }
}