package com.edx.reactive.model;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class Car extends AbstractVehicle {
    public Car() {
        setType(VehicleType.CAR);
    }
}