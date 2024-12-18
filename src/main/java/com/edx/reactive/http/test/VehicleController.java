package com.edx.reactive.http.test;

import com.edx.reactive.common.CookieScoped;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.http.CookieResponseFilter;
import com.edx.reactive.model.Car;
import com.edx.reactive.model.Motorbike;
import com.edx.reactive.model.Vehicle;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/vehicles")
@CookieScoped
public class VehicleController {

    private static final Logger log = LogManager.getLogger(VehicleController.class);
    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();

    @CookieSession
    private Vehicle userVehicle;
    @Autowired
    private CookieDataManager cookieDataManager;

    @PostMapping
    public Mono<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        log.info("Controller vehicle={}", vehicle);
        Mono<Vehicle> vehicleMono = ReactiveRequestContextHolder.getExchange()
                .flatMap(exchange -> exchange.getSession())
                .map(session -> {
                    String sessionId = session.getId();
                    String id = generateId();
                    vehicle.setId(id);
                    vehicles.put(id, vehicle);
                    userVehicle.clone(vehicle);
//                    cookieDataManager.setCookieData(sessionId, vehicle);
                    return vehicle;
                });
        vehicleMono.delayElement(Duration.ofMillis(500));
        log.info("Controller before return");
        return vehicleMono;
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
