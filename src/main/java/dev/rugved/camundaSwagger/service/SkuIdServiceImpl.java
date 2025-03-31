package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.repository.SkuIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkuIdServiceImpl implements SkuIdService {

    private final SkuIdRepository repository;

    @Autowired
    public SkuIdServiceImpl(SkuIdRepository repository) {
        this.repository = repository;
    }

    @Override
    public SkuId getSkuIdByAaUniSfp(String aaUniSfp) {
        return repository.findByAaUniSfp(aaUniSfp).orElse(null);
    }
}
