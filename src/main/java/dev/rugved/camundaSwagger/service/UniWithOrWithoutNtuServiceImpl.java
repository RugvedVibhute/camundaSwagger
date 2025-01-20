package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.UniWithOrWithoutNtuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UniWithOrWithoutNtuServiceImpl implements UniWithOrWithoutNtuService {

    @Autowired
    private UniWithOrWithoutNtuRepository repository;

    @Override
    public String getAaUniSfp(String distanceRanges, String ntuRequired, String ntuSize, String vendorType, String uniPortCapacity, String uniInterfaceType) {
        System.out.println("Executing findAaUniSfp with parameters: ");
        System.out.println("distanceRanges: " + distanceRanges);
        System.out.println("ntuRequired: " + ntuRequired);
        System.out.println("ntuSize: " + ntuSize);
        System.out.println("vendorType: " + vendorType);
        System.out.println("uniPortCapacity: " + uniPortCapacity);
        System.out.println("uniInterfaceType: " + uniInterfaceType);
        return repository.findAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);
    }
}