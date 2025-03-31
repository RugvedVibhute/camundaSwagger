package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.model.ErrorResponse;
import dev.rugved.camundaSwagger.util.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Service
public class ErrorHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerService.class);

    public Map<String, Object> handleError(Exception e, String jobType) {
        Map<String, Object> output = new HashMap<>();
        ErrorResponse errorResponse;

        if (e instanceof IllegalArgumentException) {
            errorResponse = ErrorResponse.of(
                    ErrorCodes.DATA_NOT_FOUND.getCode(),
                    e.getMessage()
            );
        } else if (e instanceof RuntimeException && e.getMessage().contains("not found")) {
            errorResponse = ErrorResponse.of(
                    ErrorCodes.DATA_NOT_FOUND.getCode(),
                    e.getMessage()
            );
        } else {
            // Handle other specific exceptions as needed
            errorResponse = ErrorResponse.of(
                    ErrorCodes.UNEXPECTED_ERROR.getCode(),
                    "An unexpected error occurred while processing " + jobType,
                    e.getMessage()
            );
        }

        logger.error("Error processing {} job: {} - {}",
                jobType, errorResponse.getErrorCode(), errorResponse.getErrorMessage(), e);

        output.put(ERROR_CODE, errorResponse.getErrorCode());
        output.put(ERROR_MESSAGE, errorResponse.getErrorMessage());
        output.put(ERROR_DETAILS, errorResponse.getErrorDetails());

        return output;
    }
}