package com.edx.reactive.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Car.class, name = "CAR"),
        @JsonSubTypes.Type(value = Motorbike.class, name = "MOTORBIKE")
})
//@JsonDeserialize(using = VehicleDeserializer.class)
public interface Vehicle {
    String getId();

    Vehicle setId(String id);

    String getColor();

    Vehicle setColor(String color);

    int getEngineCapacity();

    Vehicle setEngineCapacity(int engineCapacity);

    String getBrand();

    Vehicle setBrand(String brand);

    VehicleType getType(); // Add this method

    Vehicle setType(VehicleType type); // Add this method
}
