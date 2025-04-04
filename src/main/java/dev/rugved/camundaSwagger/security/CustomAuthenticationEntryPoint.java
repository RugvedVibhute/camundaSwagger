package au.com.optus.renaissanceCamunda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static au.com.optus.renaissanceCamunda.security.SecurityErrorConstants.*;

/**
 * Custom entry point for handling authentication failures.
 * This ensures that 401 Unauthorized errors return JSON responses
 * in the same format as other application errors.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String errorDescription = UNAUTHORIZED_DESCRIPTION_PREFIX +
                (authException.getMessage() != null ? authException.getMessage() : "Authentication required");

        // Only log at debug level for common authentication errors
        // This reduces log noise while still keeping the information available when needed
        if (errorDescription.contains("Full authentication is required") ||
                errorDescription.contains("Authentication failed") ||
                errorDescription.contains("Access is denied")) {
            logger.debug("Authentication required: {}", errorDescription);
        } else {
            // Log other, potentially more interesting authentication errors at info level
            logger.info("Authentication failure: {}", errorDescription);
        }

        // Create error response with the same format as GlobalExceptionHandler
        ErrorResponse errorResponse = new ErrorResponse(
                UNAUTHORIZED_ERROR_CODE,
                UNAUTHORIZED_ERROR_MESSAGE,
                errorDescription
        );

        // Set response status and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Write error response as JSON to the response
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}