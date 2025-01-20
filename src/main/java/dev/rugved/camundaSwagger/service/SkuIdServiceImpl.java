package dev.rugved.camundaSwagger.service.impl;

import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.repository.SkuIdRepository;
import dev.rugved.camundaSwagger.service.SkuIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkuIdServiceImpl implements SkuIdService {

    @Autowired
    private SkuIdRepository skuIdRepository;

    @Override
    public SkuId getSkuIdByAaUniSfp(String aaUniSfp) {
        return skuIdRepository.findByAaUniSfp(aaUniSfp);
    }
}
