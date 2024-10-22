package com.edx.reactive.config.vehicle;

import com.edx.reactive.model.Car;
import com.edx.reactive.model.Motorbike;
import com.edx.reactive.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class VehicleControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .responseTimeout(Duration.ofMinutes(5)) // Increase timeout to 30 seconds
                .build();
    }

    @Test
    void testVehicleEndpoints() {
        // Create a few vehicles
        Car car1 = (Car) new Car().setColor("Red").setBrand("Toyota").setEngineCapacity(1800);
        Car car2 = (Car) new Car().setColor("Blue").setBrand("Honda").setEngineCapacity(1500);
        Motorbike motorbike1 = (Motorbike) new Motorbike().setColor("Black").setBrand("Harley Davidson").setEngineCapacity(1200);
        Motorbike motorbike2 = (Motorbike) new Motorbike().setColor("Green").setBrand("Kawasaki").setEngineCapacity(1000);

        // Call the create vehicle endpoint for each vehicle
        String car1Id = createVehicle(car1);
        String car2Id = createVehicle(car2);
        String motorbike1Id = createVehicle(motorbike1);
        String motorbike2Id = createVehicle(motorbike2);

        // Call the get vehicle by ID endpoint
        WebTestClient.BodySpec<Vehicle, ?> vehicleBodySpec = getVehicleById(car1Id).expectBody(Vehicle.class);
        Vehicle responseBody = vehicleBodySpec.returnResult().getResponseBody();
        vehicleBodySpec
                .isEqualTo(car1.setId(car1Id));

        // Call the get vehicles by color endpoint
        getVehiclesByColor("Red")
                .expectBodyList(Vehicle.class)
                .contains(car1.setId(car1Id));

        // Call the get vehicles by type endpoint
        getVehiclesByType("Motorbike")
                .expectBodyList(Motorbike.class)
                .contains((Motorbike) motorbike1.setId(motorbike1Id), (Motorbike) motorbike2.setId(motorbike2Id));

        // Call the Road endpoints
        enterRoad(car1.setId(car1Id))
                .expectStatus().isOk();

        getMyVehicle()
                .expectBody(Vehicle.class)
                .isEqualTo(car1.setId(car1Id));

        getAllVehiclesOnRoad()
                .expectBodyList(Vehicle.class)
                .contains(car1.setId(car1Id));

        // Assert cookie information is valid
        String cookieValue = getCookieValue();
        assertNotNull(cookieValue);
        assertTrue(cookieValue.contains(car1Id));
    }

    private String createVehicle(Vehicle vehicle) {
        return webTestClient.post().uri("/vehicles")
                .bodyValue(vehicle)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Vehicle.class)
                .returnResult().getResponseBody().getId();
    }

    private WebTestClient.ResponseSpec getVehicleById(String id) {
        return webTestClient.get().uri("/vehicles/{id}", id)
                .exchange();
    }

    private WebTestClient.ResponseSpec getVehiclesByColor(String color) {
        return webTestClient.get().uri("/vehicles/color/{color}", color)
                .exchange();
    }

    private WebTestClient.ResponseSpec getVehiclesByType(String type) {
        return webTestClient.get().uri("/vehicles/type/{type}", type)
                .exchange();
    }

    private WebTestClient.ResponseSpec enterRoad(Vehicle vehicle) {
        return webTestClient.post().uri("/road/enter")
                .bodyValue(vehicle)
                .exchange();
    }

    private WebTestClient.ResponseSpec getMyVehicle() {
        return webTestClient.get().uri("/road/my-vehicle")
                .exchange();
    }

    private WebTestClient.ResponseSpec getAllVehiclesOnRoad() {
        return webTestClient.get().uri("/road/vehicles")
                .exchange();
    }

    private String getCookieValue() {
        return webTestClient.get().uri("/road/my-vehicle")
                .exchange()
                .returnResult(Vehicle.class)
                .getResponseCookies()
                .getFirst("session_cookie")
                .getValue();
    }
}
