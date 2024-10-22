package com.edx.reactive.config;

import com.edx.reactive.model.Vehicle;
import com.edx.reactive.model.VehicleDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Vehicle.class, new VehicleDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
