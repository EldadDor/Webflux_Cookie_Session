package com.edx.reactive.http.test;

import com.edx.reactive.model.RoadStatus;
import com.edx.reactive.model.Vehicle;
import com.edx.reactive.spring.CookieSessionBeanPostProcessor;
import com.edx.reactive.utils.RoadService;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/road")
public class RoadController {

    private final RoadService roadService;

    private static final Logger log = LogManager.getLogger(RoadController.class);

    public RoadController(RoadService roadService) {
        this.roadService = roadService;
    }


    @GetMapping("/example")
    public Mono<String> exampleMethod(@Parameter(description = "Session cookie", required = true)
                                      @CookieValue(name = "session_cookie", required = false) String sessionCookie) {
        log.info("sessionCookie=" + sessionCookie);
        return Mono.justOrEmpty(sessionCookie);
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
