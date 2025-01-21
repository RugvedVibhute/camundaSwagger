package dev.rugved.camundaSwagger.service;

public interface NtuNniSfpOrAaSfpService {
    String getNtuNniSfp(String ntuSize, String distanceRanges, String vendorType);
    String getAaSfp(String ntuSize, String distanceRanges, String vendorType);
}

