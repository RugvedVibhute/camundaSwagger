package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.NtuNniSfpOrAaSfp;
import java.util.List;

public interface NtuNniSfpOrAaSfpService {
    List<NtuNniSfpOrAaSfp> getMatchingData(String ntuSize, String distanceRanges, String vendorType);
}
