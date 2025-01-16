package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.service.NetworkElementTypeService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HardwareToBeShipped {

    @Autowired
    private NetworkElementTypeService service;

    @JobWorker(type = "HardwareToBeShipped")
    public void hardwareToBeShipped(final JobClient client, final ActivatedJob job) throws JsonProcessingException {

        // Fetch the variables from the job
        String var = job.getVariables();
        System.out.println("Job Variables: " + var);

        // Parse JSON using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(var);

        // Navigate to "networkElement" value
        JsonNode shippingOrderItems = rootNode.path("shippingOrderItem");
        if (shippingOrderItems.isArray() && shippingOrderItems.size() > 0) {
            JsonNode productCharacteristics = shippingOrderItems.get(0)
                    .path("shipment")
                    .path("shipmentItem")
                    .get(0)
                    .path("product")
                    .path("productCharacteristic");

            for (JsonNode characteristic : productCharacteristics) {
                if ("networkElement".equals(characteristic.path("name").asText())) {
                    String networkElement = characteristic.path("value").asText();
                    System.out.println("Network Element: " + networkElement);

                    // Call the service with the networkElement value
                    String vendorType = service.getVendorType(networkElement);
                    System.out.println("Vendor Type: " + vendorType);

                    // Create a map to hold the output variables
                    Map<String, Object> output = new HashMap<>();
                    output.put("vendorType", vendorType);

                    // Complete the job and send variables back to Zeebe
                    client.newCompleteCommand(job.getKey()).variables(output).send().join();
                    System.out.println("Job completed with variables: " + output);

                    // Exit the loop after processing the first matching characteristic
                    break;
                }
            }
        }
    }
}
