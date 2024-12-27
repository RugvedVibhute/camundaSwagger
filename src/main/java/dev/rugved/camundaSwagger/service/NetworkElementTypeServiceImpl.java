package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.repository.NetworkElementTypeRepository;
import dev.rugved.camundaSwagger.entity.NetworkElementType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NetworkElementTypeServiceImpl implements NetworkElementTypeService {

    @Autowired
    private NetworkElementTypeRepository repository;

    @Override
    public String getVendorType(String networkElement) {
        return repository.findByNetworkElement(networkElement)
                .map(NetworkElementType::getVendorType)
                .orElseThrow(() -> new RuntimeException("Network Element not found"));
    }
}

