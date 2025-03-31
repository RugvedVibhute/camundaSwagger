package dev.rugved.camundaSwagger.service;

/**
 * Service interface for retrieving NTU NNI SFP and AA SFP information.
 */
public interface NtuNniSfpOrAaSfpService {

    /**
     * Retrieves the NTU NNI SFP value based on the provided parameters.
     *
     * @param ntuSize The size of the NTU
     * @param distanceRanges The distance range constraints
     * @param vendorType The vendor type
     * @return The NTU NNI SFP value or null if not found
     */
    String getNtuNniSfp(String ntuSize, String distanceRanges, String vendorType);

    /**
     * Retrieves the AA SFP value based on the provided parameters.
     *
     * @param ntuSize The size of the NTU
     * @param distanceRanges The distance range constraints
     * @param vendorType The vendor type
     * @return The AA SFP value or null if not found
     */
    String getAaSfp(String ntuSize, String distanceRanges, String vendorType);
}