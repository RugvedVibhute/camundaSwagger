package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.repository.SkuIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the SkuIdService interface.
 */
@Service
public class SkuIdServiceImpl implements SkuIdService {

    private static final Logger logger = LoggerFactory.getLogger(SkuIdServiceImpl.class);
    private final SkuIdRepository repository;

    @Autowired
    public SkuIdServiceImpl(SkuIdRepository repository) {
        this.repository = repository;
    }

    @Override
    public SkuId getSkuIdByAaUniSfp(String aaUniSfp) {
        if (aaUniSfp == null || aaUniSfp.trim().isEmpty()) {
            logger.warn("Empty or null AA UNI SFP provided");
            return null;
        }

        logger.debug("Retrieving SKU ID for AA UNI SFP: {}", aaUniSfp);

        SkuId skuId = repository.findByAaUniSfp(aaUniSfp).orElse(null);

        if (skuId == null) {
            logger.warn("No SKU ID found for AA UNI SFP: {}", aaUniSfp);
        } else {
            logger.debug("Found SKU ID: {} for AA UNI SFP: {}", skuId.getAaUniSfpSkuId(), aaUniSfp);
        }

        return skuId;
    }
}