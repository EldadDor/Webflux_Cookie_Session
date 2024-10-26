package com.edx.reactive.model;

import java.util.Objects;

public abstract class AbstractVehicle implements Vehicle {
    protected VehicleType type;
    protected String id;
    protected String color;
    protected int engineCapacity;
    protected String brand;

    @Override
    public VehicleType getType() {
        return type;
    }

    @Override
    public Vehicle setType(VehicleType type) {
        this.type = type;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Vehicle setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public Vehicle setColor(String color) {
        this.color = color;
        return this;
    }

    @Override
    public int getEngineCapacity() {
        return engineCapacity;
    }

    @Override
    public Vehicle setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
        return this;
    }

    @Override
    public String getBrand() {
        return brand;
    }

    @Override
    public Vehicle setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractVehicle that = (AbstractVehicle) o;
        return engineCapacity == that.engineCapacity && type == that.type && Objects.equals(id, that.id) && Objects.equals(color, that.color) && Objects.equals(brand, that.brand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, color, engineCapacity, brand);
    }
}
