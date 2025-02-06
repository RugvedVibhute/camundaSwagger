package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.rugved.camundaSwagger.entity.NtuType;
import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.service.*;
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

    @Autowired
    private UniWithOrWithoutNtuService uniService;

    @Autowired
    private SkuIdService skuIdService;

    @Autowired
    private NtuNniSfpOrAaSfpService ntuNniSfpOrAaSfpService;

    @Autowired
    private NtuTypeService ntuTypeService;

    @JobWorker(type = "HardwareToBeShipped", tenantIds = "Infosys")
    public void hardwareToBeShipped(final JobClient client, final ActivatedJob job) throws JsonProcessingException {
        // Fetch the variables from the job
        String var = job.getVariables();
        String networkElement = job.getVariable("networkElement").toString();
        String distance = job.getVariable("distance").toString();
        String ntuRequired = job.getVariable("ntuRequired").toString();
        String ntuSize = job.getVariable("ntuSize").toString();
        String uniPortCapacity = job.getVariable("uniPortCapacity").toString();
        String uniInterfaceType = job.getVariable("uniInterfaceType").toString();
        System.out.println("Job Variables: " + var);

        Map<String, Object> output = new HashMap<>();

        try {
            if ("No".equalsIgnoreCase(ntuRequired)) {
                String distanceRanges = mapDistanceToDatabaseValue(distance);
                String vendorType = (networkElement != null) ? service.getVendorType(networkElement) : null;

                if (vendorType == null ) {
                    throw new IllegalArgumentException("No Vendor Type found for networkElement :" +networkElement);
                }

                String skuIdValue = null;

                String aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, "0", vendorType, uniPortCapacity, uniInterfaceType);

                if (aaUniSfp == null ) {
                    throw new IllegalArgumentException("No aaUniSfp found for :" +distanceRanges +", ntuSize: "+"0" + ", vendorType: " +vendorType + ", uniPortCapacity: " +uniPortCapacity +", uniInterfaceType: " +uniInterfaceType);
                }

                SkuId skuId = skuIdService.getSkuIdByAaUniSfp(aaUniSfp);

                skuIdValue =  skuId.getAaUniSfpSkuId();

                if(skuIdValue == null){
                    throw new IllegalArgumentException("No skuId found for aaUniSfp :" +aaUniSfp);
                }

                output.put("aaUniSfp", aaUniSfp);
                output.put("skuId", skuIdValue);
                output.put("ntuRequired", ntuRequired);

            } else if ("Yes".equalsIgnoreCase(ntuRequired)) {
                String ntuTypeValue = null, ntuTypeSkuIdValue = null, ntuNniSfp = null;
                String ntuNniSfpSkuIdValue = null, aaSfp = null, aaSfpSkuIdValue = null, aaUniSfp = null, skuIdValue = null;

                if (ntuSize != null) {
                    NtuType ntuType = ntuTypeService.getNtuTypeBySize(ntuSize);
                    if (ntuType == null) {
                        throw new IllegalArgumentException("No NTU Type found for size: " + ntuSize);
                    }

                    ntuTypeValue = ntuType.getNtuType();
                    SkuId ntuTypeSkuId = skuIdService.getSkuIdByAaUniSfp(ntuTypeValue);
                    ntuTypeSkuIdValue = ntuTypeSkuId.getAaUniSfpSkuId();
                    if (ntuTypeSkuIdValue == null) {
                        throw new IllegalArgumentException("No ntuTypeSkuIdValue found for ntuTypeValue : " + ntuTypeValue);
                    }
                }

                String vendorType = service.getVendorType(networkElement);
                if (distance != null && ntuSize != null && uniPortCapacity != null && uniInterfaceType != null) {
                    String distanceRanges = mapDistanceToDatabaseValue(distance);

                    ntuNniSfp = ntuNniSfpOrAaSfpService.getNtuNniSfp(ntuSize, distanceRanges, vendorType);

                    if (ntuNniSfp == null) {
                        throw new IllegalArgumentException("No ntuNniSfp found for ntuNniSfp : " + ntuNniSfp +", distanceRanges: "+ distanceRanges + ", vendorType: "+ vendorType);
                    }

                    SkuId ntuNniSfpSkuId = skuIdService.getSkuIdByAaUniSfp(ntuNniSfp);
                    ntuNniSfpSkuIdValue = ntuNniSfpSkuId.getAaUniSfpSkuId();

                    if (ntuNniSfpSkuIdValue == null) {
                        throw new IllegalArgumentException("No ntuNniSfpSkuId found for ntuNniSfp : " + ntuNniSfp);
                    }

                    aaSfp = ntuNniSfpOrAaSfpService.getAaSfp(ntuSize, distanceRanges, vendorType);
                    SkuId aaSfpSkuId = skuIdService.getSkuIdByAaUniSfp(aaSfp);
                    aaSfpSkuIdValue = aaSfpSkuId.getAaUniSfpSkuId();

                    if (aaSfpSkuIdValue == null) {
                        throw new IllegalArgumentException("No ntuNniSfpSkuId found for ntuNniSfp : " + ntuNniSfp);
                    }

                    aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);
                    if (aaUniSfp != null) {
                        SkuId skuId = skuIdService.getSkuIdByAaUniSfp(aaUniSfp);
                        skuIdValue = skuId.getAaUniSfpSkuId();
                        if (skuIdValue == null) {
                            throw new IllegalArgumentException("No skuId found for aaUniSfp : " + aaUniSfp);
                        }
                    }
                }

                output.put("ntuType", ntuTypeValue);
                output.put("ntuTypeSkuId", ntuTypeSkuIdValue);
                output.put("ntuNniSfp", ntuNniSfp);
                output.put("ntuNniSfpSkuId", ntuNniSfpSkuIdValue);
                output.put("aaSfp", aaSfp);
                output.put("aaSfpSkuId", aaSfpSkuIdValue);
                output.put("aaUniSfp", aaUniSfp);
                output.put("skuId", skuIdValue);
                output.put("ntuRequired", ntuRequired);
            }

            output.put("errorMessage", null);
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            System.out.println("Job completed with variables: " + output);

        } catch (Exception e) {
            System.err.println("Error processing HardwareToBeShipped: " + e.getMessage());

            // Return error variables instead of stopping the BPMN process
            output.clear();
            output.put("errorMessage", " " + e.getMessage());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }

    private String mapDistanceToDatabaseValue(String distance) {
        try {
            int numericDistance = Integer.parseInt(distance.trim());

            if (numericDistance < 100) {
                return "distance_ranges = '<100' OR distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 100 && numericDistance < 300) {
                return "distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 300 && numericDistance < 500) {
                return "distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance < 10000) {
                return "distance_ranges = '<10000'";
            } else {
                return "distance_ranges = 'incorrect distance value'";
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid distance value: " + distance);
            return null;
        }
    }

}
