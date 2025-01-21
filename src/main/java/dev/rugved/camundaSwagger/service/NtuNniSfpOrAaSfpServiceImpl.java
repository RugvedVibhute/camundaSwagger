package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.NtuNniSfpOrAaSfp;
import dev.rugved.camundaSwagger.repository.NtuNniSfpOrAaSfpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NtuNniSfpOrAaSfpServiceImpl implements NtuNniSfpOrAaSfpService {

    @Autowired
    private NtuNniSfpOrAaSfpRepository repository;

    @Override
    public List<NtuNniSfpOrAaSfp> getMatchingData(String ntuSize, String distanceRanges, String vendorType) {
        return repository.findMatchingData(ntuSize, distanceRanges, vendorType);
    }
}

