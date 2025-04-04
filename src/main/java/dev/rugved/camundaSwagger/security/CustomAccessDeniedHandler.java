package au.com.optus.renaissanceCamunda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static au.com.optus.renaissanceCamunda.security.SecurityErrorConstants.*;

/**
 * Custom handler for access denied exceptions (403 Forbidden).
 * This ensures that 403 Forbidden errors return JSON responses
 * in the same format as other application errors.
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String errorDescription = FORBIDDEN_DESCRIPTION_PREFIX +
                (accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Access is denied");

        // Log at debug level to reduce log noise
        logger.debug("Access denied: {}", errorDescription);

        // Create error response with the same format as GlobalExceptionHandler
        ErrorResponse errorResponse = new ErrorResponse(
                FORBIDDEN_ERROR_CODE,
                FORBIDDEN_ERROR_MESSAGE,
                errorDescription
        );

        // Set response status and content type
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Write error response as JSON to the response
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}