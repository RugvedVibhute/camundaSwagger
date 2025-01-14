package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.WBSHeaderRepository;
import dev.rugved.camundaSwagger.entity.WBSHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WBSHeaderServiceImpl implements WBSHeaderService {

    @Autowired
    private WBSHeaderRepository repository;

    @Override
    public WBSHeader getWBSHeaderDetailsByState(String stateOrProvince) {
        return repository.findByStateOrProvince(stateOrProvince)
                .orElseThrow(() -> new RuntimeException("WBS Header not found for state or province: " + stateOrProvince));
    }
}