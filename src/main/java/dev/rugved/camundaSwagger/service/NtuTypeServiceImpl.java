package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NtuTypeRepository;
import dev.rugved.camundaSwagger.entity.NtuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the NtuTypeService interface.
 */
@Service
public class NtuTypeServiceImpl implements NtuTypeService {

    private static final Logger logger = LoggerFactory.getLogger(NtuTypeServiceImpl.class);
    private final NtuTypeRepository repository;

    @Autowired
    public NtuTypeServiceImpl(NtuTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public NtuType getNtuTypeBySize(String ntuSize) {
        if (ntuSize == null || ntuSize.trim().isEmpty()) {
            logger.warn("Empty or null NTU size provided");
            return null;
        }

        logger.debug("Retrieving NTU Type for size: {}", ntuSize);

        NtuType ntuType = repository.findByNtuSize(ntuSize)
                .orElse(null);

        if (ntuType == null) {
            logger.warn("No NTU Type found for size: {}", ntuSize);
        } else {
            logger.debug("Found NTU Type: {} for size: {}", ntuType.getNtuType(), ntuSize);
        }

        return ntuType;
    }
}