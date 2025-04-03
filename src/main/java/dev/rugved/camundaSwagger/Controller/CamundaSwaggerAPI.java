package dev.rugved.camundaSwagger.Controller;

import dev.rugved.camundaSwagger.dto.StartProcessRequest;
import dev.rugved.camundaSwagger.exception.BadRequestException;
import dev.rugved.camundaSwagger.exception.ResourceNotFoundException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/process")
public class CamundaSwaggerAPI implements CommandLineRunner {

    @Autowired
    private ZeebeClient zeebeClient;

    private static final Logger LOG = LoggerFactory.getLogger(CamundaSwaggerAPI.class);

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody StartProcessRequest requestBody) {
        try {
            // Explicit null checks before validation
            if (requestBody == null) {
                throw new BadRequestException("Request body cannot be null");
            }

            String bpmnProcessId = requestBody.getBpmnProcessId();
            Map<String, Object> variables = requestBody.getVariables();

            // Validate input parameters
            if (StringUtils.isBlank(bpmnProcessId)) {
                throw new BadRequestException("Invalid input parameter: bpmnProcessId cannot be null or empty");
            }

            if (variables == null) {
                throw new BadRequestException("Invalid input parameter: variables cannot be null");
            }

            // Start process instance
            try {
                final ProcessInstanceEvent processInstanceEvent =
                        zeebeClient
                                .newCreateInstanceCommand()
                                .bpmnProcessId(bpmnProcessId)
                                .latestVersion()
                                .variables(variables)
                                .send()
                                .join();

                // Log and return success response
                long processInstanceKey = processInstanceEvent.getProcessInstanceKey();
                LOG.info("Process instance created: " + processInstanceKey);

                return ResponseEntity.ok(
                        Map.of("message", "Process instance started successfully",
                                "processInstanceKey", processInstanceKey));
            } catch (Exception e) {
                // Handle specific Zeebe client exceptions
                return handleZeebeException(e);
            }
        } catch (BadRequestException e) {
            // Re-throw BadRequestException to be handled by GlobalExceptionHandler
            throw e;
        } catch (Exception e) {
            // Let the GlobalExceptionHandler handle all other exceptions
            LOG.error("Unexpected error processing request", e);
            throw e;
        }
    }

    /**
     * Helper method to convert Zeebe exceptions to our custom exceptions
     */
    private RuntimeException handleZeebeException(Exception e) {
        String errorMessage = e.getMessage();
        Throwable cause = e.getCause();

        LOG.error("Error in Zeebe operation: " + errorMessage, e);

        // Handle Zeebe client status exceptions
        if (cause instanceof ClientStatusException) {
            ClientStatusException clientException = (ClientStatusException) cause;
            String errorDetails = clientException.getMessage();

            if (errorDetails.contains("NOT_FOUND")) {
                if (errorDetails.contains("Expected to find process definition")) {
                    throw new ResourceNotFoundException("Process definition not found: " +
                            errorDetails.replaceAll(".*process ID '([^']+)'.*", "$1"));
                }
                throw new ResourceNotFoundException(errorDetails);
            }

            if (errorDetails.contains("INVALID_ARGUMENT")) {
                throw new BadRequestException(errorDetails);
            }

            if (errorDetails.contains("PERMISSION_DENIED")) {
                throw new dev.rugved.camundaSwagger.exception.ForbiddenException(errorDetails);
            }

            if (errorDetails.contains("UNAUTHENTICATED") || errorDetails.contains("expired")) {
                throw new dev.rugved.camundaSwagger.exception.UnauthorizedException(errorDetails);
            }
        }

        // Check for execution exceptions
        if (e instanceof ExecutionException && e.getCause() != null) {
            return handleZeebeException((Exception) e.getCause());
        }

        // For all other cases
        throw new dev.rugved.camundaSwagger.exception.ServiceUnavailableException(
                errorMessage != null ? errorMessage : "An error occurred when communicating with Zeebe engine");
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("CamundaSwaggerAPI application has started.");
    }
}