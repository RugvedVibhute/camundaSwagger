package dev.rugved.camundaSwagger.Controller;

import dev.rugved.camundaSwagger.dto.StartProcessRequest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/process")
public class CamundaSwaggerAPI implements CommandLineRunner {

    @Autowired
    private ZeebeClient zeebeClient;

    private static final Logger LOG = LoggerFactory.getLogger(CamundaSwaggerAPI.class);

    @PostMapping("/start")
    public ResponseEntity<?> start(@Valid @RequestBody StartProcessRequest requestBody) {
        try {
            // Validate input
            String bpmnProcessId = requestBody.getBpmnProcessId();
            Map<String, Object> variables = requestBody.getVariables();

            // Start process instance
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
            // Let the GlobalExceptionHandler handle all exceptions
            throw handleZeebeException(e);
        }
    }

    /**
     * Helper method to convert Zeebe exceptions to our custom exceptions
     */
    private RuntimeException handleZeebeException(Exception e) {
        String errorMessage = e.getMessage();
        LOG.error("Error in Zeebe operation: " + errorMessage, e);

        if (errorMessage.contains("NOT_FOUND") || errorMessage.contains("not found")) {
            return new dev.rugved.camundaSwagger.exception.ResourceNotFoundException(errorMessage);
        }

        if (errorMessage.contains("INVALID_ARGUMENT")) {
            return new dev.rugved.camundaSwagger.exception.BadRequestException(errorMessage);
        }

        if (errorMessage.contains("PERMISSION_DENIED") || errorMessage.contains("permission denied")) {
            return new dev.rugved.camundaSwagger.exception.ForbiddenException(errorMessage);
        }

        return new dev.rugved.camundaSwagger.exception.ServiceUnavailableException(errorMessage);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("CamundaSwaggerAPI application has started.");
    }
}