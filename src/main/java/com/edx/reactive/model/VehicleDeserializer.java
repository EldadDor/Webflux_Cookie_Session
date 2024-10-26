package com.edx.reactive.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * If needed some better customization could use this JsonDeserializer
 */
public class VehicleDeserializer extends JsonDeserializer<Vehicle> {
    @Override
    public Vehicle deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        String type = node.get("type").asText();
        Vehicle vehicle = switch (type) {
            case "CAR" -> new Car();
            case "MOTORBIKE" -> new Motorbike();
            case null, default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };

        vehicle.setType(VehicleType.valueOf(type));
        vehicle.setColor(node.get("color").asText());
        vehicle.setBrand(node.get("brand").asText());
        vehicle.setEngineCapacity(node.get("engineCapacity").asInt());

        return vehicle;
    }
}
