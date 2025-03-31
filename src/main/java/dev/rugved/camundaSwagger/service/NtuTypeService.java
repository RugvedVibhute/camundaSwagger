package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.NtuType;

/**
 * Service interface for retrieving NTU Type information.
 */
public interface NtuTypeService {

    /**
     * Retrieves the NTU Type entity associated with the specified NTU size.
     *
     * @param ntuSize The size of the NTU
     * @return The NtuType entity associated with the specified size, or null if not found
     */
    NtuType getNtuTypeBySize(String ntuSize);
}