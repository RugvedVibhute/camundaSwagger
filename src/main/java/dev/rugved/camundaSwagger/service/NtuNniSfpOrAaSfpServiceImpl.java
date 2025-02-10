package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NtuNniSfpOrAaSfpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NtuNniSfpOrAaSfpServiceImpl implements NtuNniSfpOrAaSfpService {

    private static final Logger logger = LoggerFactory.getLogger(NtuNniSfpOrAaSfpServiceImpl.class);

    @Autowired
    private final NtuNniSfpOrAaSfpRepository repository;
    
    public NtuNniSfpOrAaSfpServiceImpl(NtuNniSfpOrAaSfpRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getNtuNniSfp(String ntuSize, String distanceRanges, String vendorType) {
        logger.info("NtuNniSfp ntuSize: {}" , ntuSize);
        logger.info("NtuNniSfp distanceRanges: {}" , distanceRanges);
        logger.info("NtuNniSfp vendorType: {}" , vendorType);
        return repository.findNtuNniSfp(ntuSize, distanceRanges, vendorType);
    }

    @Override
    public String getAaSfp(String ntuSize, String distanceRanges, String vendorType) {
        logger.info("AaSfp ntuSize: {}" , ntuSize);
        logger.info("AaSfp distanceRanges: {}" , distanceRanges);
        logger.info("AaSfp vendorType: {}" , vendorType);
        return repository.findAaSfp(ntuSize, distanceRanges, vendorType);
    }
}
