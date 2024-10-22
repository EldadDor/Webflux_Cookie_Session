package com.edx.reactive.utils;

import com.edx.reactive.common.CookieSession;
import com.edx.reactive.model.RoadStatus;
import com.edx.reactive.model.Vehicle;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoadService {

    @CookieSession
    private Vehicle userVehicle;

    private final Map<String, Vehicle> vehiclesOnRoad = new ConcurrentHashMap<>();

    public Mono<RoadStatus> getRoadStatus() {
        return Mono.just(new RoadStatus(vehiclesOnRoad.size()));
    }

    public Mono<Void> addVehicleToRoad(Vehicle vehicle) {
        return Mono.fromRunnable(() -> {
            vehiclesOnRoad.put(vehicle.getId(), vehicle);
            userVehicle = vehicle; // This will trigger the cookie update
        });
    }

    public Mono<Vehicle> getUserVehicle() {
        return Mono.justOrEmpty(userVehicle);
    }

    public Flux<Vehicle> getAllVehiclesOnRoad() {
        return Flux.fromIterable(vehiclesOnRoad.values());
    }
}
