package com.hospital.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-wide bean definitions (ModelMapper, etc.).
 */
@Configuration
public class AppConfig {

    /**
     * Strict ModelMapper: only maps fields whose names match exactly.
     * Prevents accidental mapping of similarly named but unrelated fields.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
