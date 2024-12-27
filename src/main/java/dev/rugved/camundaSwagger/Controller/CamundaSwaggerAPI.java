package dev.rugved.camundaSwagger.Controller;

import dev.rugved.camundaSwagger.dto.StartProcessRequest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
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
            LOG.info("Process instance created: "+ processInstanceKey);

            return ResponseEntity.ok(
                    Map.of("message", "Process instance started successfully",
                            "processInstanceKey", processInstanceKey));

        }
        catch (Exception e) {


            // Log and return error response
            LOG.error("Error starting process instance"+ e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start process instance", "details", e.getMessage()));
        }
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("CamundaSwaggerAPI application has started.");
    }
}

