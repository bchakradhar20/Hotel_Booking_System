package com.hotelreservation.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.*;

/**
 * Spring configuration class for ModelMapper bean setup.
 * Provides a shared ModelMapper instance for DTO-to-entity and entity-to-DTO conversions.
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Creates a ModelMapper bean with STRICT matching strategy.
     * STRICT strategy ensures only exact property name matches are mapped,
     * preventing accidental field mappings in complex object hierarchies.
     *
     * @return configured ModelMapper bean
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        // STRICT matching prevents partial or ambiguous field mappings
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
