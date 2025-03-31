package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.WBSHeaderRepository;
import dev.rugved.camundaSwagger.entity.WBSHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the WBSHeaderService interface.
 */
@Service
public class WBSHeaderServiceImpl implements WBSHeaderService {

    private static final Logger logger = LoggerFactory.getLogger(WBSHeaderServiceImpl.class);
    private final WBSHeaderRepository repository;

    @Autowired
    public WBSHeaderServiceImpl(WBSHeaderRepository repository) {
        this.repository = repository;
    }

    @Override
    public WBSHeader getWBSHeaderDetailsByState(String stateOrProvince) {
        if (stateOrProvince == null || stateOrProvince.trim().isEmpty()) {
            logger.warn("Empty or null stateOrProvince provided");
            return null;
        }

        logger.debug("Retrieving WBS Header for state: {}", stateOrProvince);

        WBSHeader wbsHeader = repository.findByStateOrProvince(stateOrProvince)
                .orElse(null);

        if (wbsHeader == null) {
            logger.warn("No WBS Header found for state: {}", stateOrProvince);
            throw new RuntimeException("WBS Header not found for state or province: " + stateOrProvince);
        } else {
            logger.debug("Found WBS Header: {} for state: {}", wbsHeader.getWbsHeader(), stateOrProvince);
        }

        return wbsHeader;
    }
}