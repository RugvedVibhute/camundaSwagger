package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.NtuType;

public interface NtuTypeService {
    NtuType getNtuTypeBySize(String ntuSize);
}
