package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.NtuType;
import dev.rugved.camundaSwagger.repository.NtuTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NtuTypeServiceImpl implements NtuTypeService {

    @Autowired
    private NtuTypeRepository ntuTypeRepository;

    @Override
    public NtuType getNtuTypeBySize(String ntuSize) {
        Optional<NtuType> ntuType = ntuTypeRepository.findByNtuSize(ntuSize);
        return ntuType.orElseThrow(() -> new IllegalArgumentException("No NTU Type found for ntuSize: " + ntuSize));
    }
}

