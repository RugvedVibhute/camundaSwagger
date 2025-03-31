package dev.rugved.camundaSwagger.service;

/**
 * Service interface for retrieving network element type information.
 */
public interface NetworkElementTypeService {

    /**
     * Retrieves the vendor type associated with the specified network element.
     *
     * @param networkElement The network element identifier
     * @return The vendor type associated with the network element, or null if not found
     */
    String getVendorType(String networkElement);
}