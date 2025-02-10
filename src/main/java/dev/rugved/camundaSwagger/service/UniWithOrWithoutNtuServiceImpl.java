package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.UniWithOrWithoutNtuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UniWithOrWithoutNtuServiceImpl implements UniWithOrWithoutNtuService {

    private static final Logger logger = LoggerFactory.getLogger(UniWithOrWithoutNtuServiceImpl.class);

    private final UniWithOrWithoutNtuRepository repository;

    @Autowired
    public UniWithOrWithoutNtuServiceImpl(UniWithOrWithoutNtuRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getAaUniSfp(String distanceRanges, String ntuRequired, String ntuSize, String vendorType, String uniPortCapacity, String uniInterfaceType) {
        logger.info("Executing findAaUniSfp with parameters: ");
        logger.info("distanceRanges: {}", distanceRanges);
        logger.info("ntuRequired: {}" , ntuRequired);
        logger.info("ntuSize: {}" , ntuSize);
        logger.info("vendorType: {}" , vendorType);
        logger.info("uniPortCapacity: {}" , uniPortCapacity);
        logger.info("uniInterfaceType: {}" , uniInterfaceType);
        return repository.findAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);
    }
}
