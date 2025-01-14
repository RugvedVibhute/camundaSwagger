package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.WBSHeader;

public interface WBSHeaderService {
    WBSHeader getWBSHeaderDetailsByState(String stateOrProvince);
}