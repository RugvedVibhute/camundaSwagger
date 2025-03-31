package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.SkuId;

/**
 * Service interface for retrieving SKU ID information.
 */
public interface SkuIdService {

    /**
     * Retrieves the SKU ID entity associated with the specified AA UNI SFP identifier.
     *
     * @param aaUniSfp The AA UNI SFP identifier
     * @return The SkuId entity associated with the AA UNI SFP, or null if not found
     */
    SkuId getSkuIdByAaUniSfp(String aaUniSfp);
}