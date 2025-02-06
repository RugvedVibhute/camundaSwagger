package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NtuTypeRepository;
import dev.rugved.camundaSwagger.entity.NtuType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NtuTypeServiceImpl implements NtuTypeService {

    private final NtuTypeRepository repository;

    @Autowired
    public NtuTypeServiceImpl(NtuTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public NtuType getNtuTypeBySize(String ntuSize) {
        return repository.findByNtuSize(ntuSize)
                .orElseThrow(() -> new RuntimeException("NtuType not found for size: " + ntuSize));
    }
}
