package dev.rugved.camundaSwagger.service;

/**
 * Service interface for working with UNI (User Network Interface) configurations
 * with or without NTU (Network Termination Unit).
 */
public interface UniWithOrWithoutNtuService {

    /**
     * Retrieves the appropriate AA UNI SFP based on the provided parameters.
     *
     * @param distanceRanges    The distance range constraint
     * @param ntuRequired       Whether NTU is required ("Yes" or "No")
     * @param ntuSize           The size of the NTU (if required)
     * @param vendorType        The vendor type
     * @param uniPortCapacity   The UNI port capacity
     * @param uniInterfaceType  The UNI interface type
     * @return The AA UNI SFP identifier or null if not found
     */
    String getAaUniSfp(String distanceRanges, String ntuRequired, String ntuSize,
                       String vendorType, String uniPortCapacity, String uniInterfaceType);
}