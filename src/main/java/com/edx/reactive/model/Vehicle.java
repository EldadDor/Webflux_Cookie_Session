package com.edx.reactive.model;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.utils.CglibProxyFactory;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.reflect.Method;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Car.class, name = "CAR"),
        @JsonSubTypes.Type(value = Motorbike.class, name = "MOTORBIKE")
})
//@JsonDeserialize(using = VehicleDeserializer.class)
public interface Vehicle extends CookieData {
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

    default Vehicle clone(Vehicle source) {
        if (source == null) {
            throw new IllegalArgumentException("Target vehicle cannot be null");
        }

        // Get the real source object if it's a proxy
        Vehicle target = CglibProxyFactory.isProxy(this) ? CglibProxyFactory.getTargetObject(this) : this;

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        for (Method method : sourceClass.getMethods()) {
            if (isGetter(method)) {
                String propertyName = method.getName().substring(3);
                try {
                    Method setter = targetClass.getMethod("set" + propertyName, method.getReturnType());
                    Object value = method.invoke(source);
                    setter.invoke(target, value);
                } catch (Exception e) {
                    // Log the exception or handle it as appropriate for your application
                    System.err.println("Error copying property: " + propertyName);
                }
            }
        }
        return target;
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get") &&
                method.getParameterCount() == 0 &&
                !method.getDeclaringClass().getName().equals("java.lang.Object") &&
                !method.getReturnType().equals(void.class);
    }
}
