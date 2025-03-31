package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.WBSHeader;

/**
 * Service interface for retrieving WBS Header information.
 */
public interface WBSHeaderService {

    /**
     * Retrieves the WBS Header entity associated with the specified state or province.
     *
     * @param stateOrProvince The state or province to search for
     * @return The WBSHeader entity associated with the state or province, or null if not found
     */
    WBSHeader getWBSHeaderDetailsByState(String stateOrProvince);
}