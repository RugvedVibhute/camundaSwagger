package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.UniWithOrWithoutNtuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the UniWithOrWithoutNtuService interface.
 */
@Service
public class UniWithOrWithoutNtuServiceImpl implements UniWithOrWithoutNtuService {

    private static final Logger logger = LoggerFactory.getLogger(UniWithOrWithoutNtuServiceImpl.class);
    private final UniWithOrWithoutNtuRepository repository;

    @Autowired
    public UniWithOrWithoutNtuServiceImpl(UniWithOrWithoutNtuRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getAaUniSfp(String distanceRanges, String ntuRequired, String ntuSize,
                              String vendorType, String uniPortCapacity, String uniInterfaceType) {
        if (distanceRanges == null || vendorType == null || uniPortCapacity == null || uniInterfaceType == null) {
            logger.warn("One or more required parameters are null: distanceRanges={}, vendorType={}, " +
                            "uniPortCapacity={}, uniInterfaceType={}",
                    distanceRanges, vendorType, uniPortCapacity, uniInterfaceType);
            return null;
        }

        if (ntuRequired == null) {
            logger.warn("ntuRequired parameter is null");
            return null;
        }

        // For "Yes" NTU case, ntuSize is required
        if ("Yes".equalsIgnoreCase(ntuRequired) && (ntuSize == null || ntuSize.trim().isEmpty())) {
            logger.warn("ntuSize parameter is required when ntuRequired is 'Yes'");
            return null;
        }

        logger.debug("Searching for AA UNI SFP with parameters: distanceRanges={}, ntuRequired={}, " +
                        "ntuSize={}, vendorType={}, uniPortCapacity={}, uniInterfaceType={}",
                distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);

        String aaUniSfp = repository.findAaUniSfp(distanceRanges, ntuRequired, ntuSize,
                vendorType, uniPortCapacity, uniInterfaceType);

        if (aaUniSfp == null) {
            logger.warn("No AA UNI SFP found for the given parameters");
        } else {
            logger.debug("Found AA UNI SFP: {}", aaUniSfp);
        }

        return aaUniSfp;
    }
}