package com.edx.reactive.http.test;

import com.edx.reactive.common.CookieSession;
import com.edx.reactive.model.Car;
import com.edx.reactive.model.Motorbike;
import com.edx.reactive.model.Vehicle;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();

    @CookieSession
    private Vehicle userVehicle;

    @PostMapping
    public Mono<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        String id = generateId();
        vehicle.setId(id);
        vehicles.put(id, vehicle);
        userVehicle = vehicle;
        return Mono.just(vehicle);
    }

    @GetMapping("/{id}")
    public Mono<Vehicle> getVehicleById(@PathVariable String id) {
        return Mono.justOrEmpty(vehicles.get(id));
    }

    @GetMapping("/color/{color}")
    public Flux<Vehicle> getVehiclesByColor(@PathVariable String color) {
        return Flux.fromStream(vehicles.values().stream().filter(vehicle -> vehicle.getColor().equalsIgnoreCase(color)));
    }

    @GetMapping("/type/{type}")
    public Flux<Vehicle> getVehiclesByType(@PathVariable String type) {
        Class<?> vehicleType = getVehicleType(type);
        return Flux.fromStream(vehicles.values().stream().filter(vehicle -> vehicleType.isInstance(vehicle)));
    }

    private String generateId() {
        // Generate a unique ID for the vehicle
        return UUID.randomUUID().toString();
    }

    private Class<?> getVehicleType(String type) {
        if (type.equalsIgnoreCase("car")) {
            return Car.class;
        } else if (type.equalsIgnoreCase("motorbike")) {
            return Motorbike.class;
        } else {
            throw new IllegalArgumentException("Invalid vehicle type: " + type);
        }
    }
}
