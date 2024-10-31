package com.edx.reactive.config.vehicle;

import com.edx.reactive.CookieInjectionApplication;
import com.edx.reactive.model.Car;
import com.edx.reactive.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = {CookieInjectionApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class VehicleFlowTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testVehicleFlow() {
        // Create a car vehicle
        Car car = new Car();
        car.setColor("Red");
        car.setBrand("Toyota");
        car.setEngineCapacity(1800);

        // Send POST request to create the vehicle
        webTestClient.post().uri("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(car)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Vehicle.class)
                .value(createdVehicle -> {
                    // Assert the created vehicle properties
                    assert createdVehicle.getColor().equals(car.getColor());
                    assert createdVehicle.getBrand().equals(car.getBrand());
                    assert createdVehicle.getEngineCapacity() == car.getEngineCapacity();
                })
                .consumeWith(response -> {
                    // Check for the existence of the cookie
                    String cookieValue = response.getResponseCookies().getFirst("session_cookie").getValue();
                    assert cookieValue != null && !cookieValue.isEmpty();

                    // Decrypt and verify cookie content
                    String decryptedValue = decryptCookieValue(cookieValue);
                    assert decryptedValue.contains(car.getColor());
                    assert decryptedValue.contains(car.getBrand());
                    assert decryptedValue.contains(String.valueOf(car.getEngineCapacity()));
                });
    }

    // Helper method to decrypt the cookie value
    private String decryptCookieValue(String encryptedValue) {
        // Implement the logic to decrypt the cookie value
        // This could involve calling the CookieEncryptionService
        return "decrypted_value";
    }

    // Helper method to set the @CookieSession object
    private void setSessionVehicle(Vehicle vehicle) {
        // Implement the logic to set the @CookieSession object
        // This could involve calling a service method or directly setting the field
    }

    // Helper method to decrypt the cookie value

}
