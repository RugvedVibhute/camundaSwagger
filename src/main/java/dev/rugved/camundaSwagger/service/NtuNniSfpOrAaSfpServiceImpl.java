package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NtuNniSfpOrAaSfpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the NtuNniSfpOrAaSfpService interface.
 */
@Service
public class NtuNniSfpOrAaSfpServiceImpl implements NtuNniSfpOrAaSfpService {

    private static final Logger logger = LoggerFactory.getLogger(NtuNniSfpOrAaSfpServiceImpl.class);
    private final NtuNniSfpOrAaSfpRepository repository;

    @Autowired
    public NtuNniSfpOrAaSfpServiceImpl(NtuNniSfpOrAaSfpRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getNtuNniSfp(String ntuSize, String distanceRanges, String vendorType) {
        if (ntuSize == null || distanceRanges == null || vendorType == null) {
            logger.warn("One or more parameters are null: ntuSize={}, distanceRanges={}, vendorType={}",
                    ntuSize, distanceRanges, vendorType);
            return null;
        }

        logger.debug("Retrieving NTU NNI SFP for: ntuSize={}, distanceRanges={}, vendorType={}",
                ntuSize, distanceRanges, vendorType);

        String ntuNniSfp = repository.findNtuNniSfp(ntuSize, distanceRanges, vendorType);

        if (ntuNniSfp == null) {
            logger.warn("No NTU NNI SFP found for the given parameters");
        } else {
            logger.debug("Found NTU NNI SFP: {}", ntuNniSfp);
        }

        return ntuNniSfp;
    }

    @Override
    public String getAaSfp(String ntuSize, String distanceRanges, String vendorType) {
        if (ntuSize == null || distanceRanges == null || vendorType == null) {
            logger.warn("One or more parameters are null: ntuSize={}, distanceRanges={}, vendorType={}",
                    ntuSize, distanceRanges, vendorType);
            return null;
        }

        logger.debug("Retrieving AA SFP for: ntuSize={}, distanceRanges={}, vendorType={}",
                ntuSize, distanceRanges, vendorType);

        String aaSfp = repository.findAaSfp(ntuSize, distanceRanges, vendorType);

        if (aaSfp == null) {
            logger.warn("No AA SFP found for the given parameters");
        } else {
            logger.debug("Found AA SFP: {}", aaSfp);
        }

        return aaSfp;
    }
}