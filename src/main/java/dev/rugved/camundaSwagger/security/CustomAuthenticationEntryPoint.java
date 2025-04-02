package au.com.optus.renaissanceCamunda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Create error response with the same format as GlobalExceptionHandler
        ErrorResponse errorResponse = new ErrorResponse(
                UNAUTHORIZED_ERROR_CODE,
                UNAUTHORIZED_ERROR_MESSAGE,
                UNAUTHORIZED_DESCRIPTION_PREFIX + authException.getMessage()
        );

        // Set response status and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Write error response as JSON to the response
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}