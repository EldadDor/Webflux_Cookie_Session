package com.edx.reactive.http.test;

import com.edx.reactive.model.RoadStatus;
import com.edx.reactive.model.Vehicle;
import com.edx.reactive.utils.RoadService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/road")
public class RoadController {

    private final RoadService roadService;

    public RoadController(RoadService roadService) {
        this.roadService = roadService;
    }

    @GetMapping("/status")
    public Mono<RoadStatus> getRoadStatus() {
        return roadService.getRoadStatus();
    }

    @PostMapping("/enter")
    public Mono<Void> enterRoad(@RequestBody Vehicle vehicle) {
        return roadService.addVehicleToRoad(vehicle);
    }

    @GetMapping("/my-vehicle")
    public Mono<Vehicle> getMyVehicle() {
        return roadService.getUserVehicle();
    }

    @GetMapping("/vehicles")
    public Flux<Vehicle> getAllVehicles() {
        return roadService.getAllVehiclesOnRoad();
    }
}
