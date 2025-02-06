package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NtuNniSfpOrAaSfpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NtuNniSfpOrAaSfpServiceImpl implements NtuNniSfpOrAaSfpService {

    private final NtuNniSfpOrAaSfpRepository repository;

    @Autowired
    public NtuNniSfpOrAaSfpServiceImpl(NtuNniSfpOrAaSfpRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getNtuNniSfp(String ntuSize, String distanceRanges, String vendorType) {
        System.out.println("service ntuSize: " + ntuSize);
        System.out.println("service distanceRanges: " + distanceRanges);
        System.out.println("service vendorType: " + vendorType);
        return repository.findNtuNniSfp(ntuSize, distanceRanges, vendorType);
    }

    @Override
    public String getAaSfp(String ntuSize, String distanceRanges, String vendorType) {
        System.out.println("service ntuSize: " + ntuSize);
        System.out.println("service distanceRanges: " + distanceRanges);
        System.out.println("service vendorType: " + vendorType);
        return repository.findAaSfp(ntuSize, distanceRanges, vendorType);
    }
}
