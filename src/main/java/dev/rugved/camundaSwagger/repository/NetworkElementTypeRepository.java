package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NetworkElementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NetworkElementTypeRepository extends JpaRepository<NetworkElementType, Long> {

    Optional<NetworkElementType> findByNetworkElement(String networkElement);
}

