package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NetworkElementTypeRepository;
import dev.rugved.camundaSwagger.entity.NetworkElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the NetworkElementTypeService interface.
 */
@Service
public class NetworkElementTypeServiceImpl implements NetworkElementTypeService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkElementTypeServiceImpl.class);
    private final NetworkElementTypeRepository repository;

    @Autowired
    public NetworkElementTypeServiceImpl(NetworkElementTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getVendorType(String networkElement) {
        if (networkElement == null || networkElement.trim().isEmpty()) {
            logger.warn("Empty or null network element provided");
            return null;
        }

        logger.debug("Retrieving vendor type for network element: {}", networkElement);

        String vendorType = repository.findByNetworkElement(networkElement)
                .map(NetworkElementType::getVendorType)
                .orElse(null);

        if (vendorType == null) {
            logger.warn("No vendor type found for network element: {}", networkElement);
        } else {
            logger.debug("Found vendor type: {} for network element: {}", vendorType, networkElement);
        }

        return vendorType;
    }
}