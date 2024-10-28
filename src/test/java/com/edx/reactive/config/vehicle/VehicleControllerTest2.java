package com.edx.reactive.config.vehicle;

import com.edx.reactive.model.Car;
import com.edx.reactive.model.Motorbike;
import com.edx.reactive.model.Vehicle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class VehicleControllerTest2 {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int THREAD_COUNT = 3;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    private final RestClient restClient = RestClient.create();

    @Test
    void testMultiThreadedVehicleCreationAndRetrieval() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<Future<TestResult>> futures = new ArrayList<>();

        // Create different vehicles for each thread
        Vehicle vehicle1 = new Car().setColor("red").setBrand("Toyota");
        Vehicle vehicle2 = new Car().setColor("blue").setBrand("Honda");
        Vehicle vehicle3 = new Car().setColor("green").setBrand("Kawasaki");
        Vehicle[] vehicles = {
                vehicle1,
                vehicle2,
                vehicle3
        };

        // Submit tasks to executor
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            Future<TestResult> future = executorService.submit(() -> {
                try {
                    return executeVehicleTest(vehicles[index]);
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);

        // Verify results
        for (Future<TestResult> future : futures) {
            TestResult result = future.get();
            assertNotNull(result.sessionCookie);
            assertEquals(result.originalVehicle.getColor(), result.retrievedVehicle.getColor());
            assertEquals(result.originalVehicle.getBrand(), result.retrievedVehicle.getBrand());
        }
    }

    private TestResult executeVehicleTest(Vehicle vehicle) {
        // Step 1: Create vehicle and get session cookie
        ResponseEntity<Vehicle> createResponse = restClient.post()
                .uri(BASE_URL + "/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .body(vehicle)
                .retrieve()
                .toEntity(Vehicle.class);

        String sessionCookie = extractSessionCookie(createResponse);
        assertNotNull(sessionCookie, "Session cookie should not be null");

        // Step 2: Get my vehicle using the session cookie
        Vehicle myVehicle = restClient.get()
                .uri(BASE_URL + "/road/my-vehicle")
                .header("Cookie", sessionCookie)
                .retrieve()
                .body(Vehicle.class);

        return new TestResult(sessionCookie, vehicle, myVehicle);
    }

    private String extractSessionCookie(ResponseEntity<?> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            return cookies.stream()
                    .filter(cookie -> cookie.startsWith("cookie_session="))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private static class TestResult {
        private final String sessionCookie;
        private final Vehicle originalVehicle;
        private final Vehicle retrievedVehicle;

        private TestResult(String sessionCookie, Vehicle originalVehicle, Vehicle retrievedVehicle) {
            this.sessionCookie = sessionCookie;
            this.originalVehicle = originalVehicle;
            this.retrievedVehicle = retrievedVehicle;
        }

        public String getSessionCookie() {
            return sessionCookie;
        }

        public Vehicle getOriginalVehicle() {
            return originalVehicle;
        }

        public Vehicle getRetrievedVehicle() {
            return retrievedVehicle;
        }
    }

    @AfterEach
    void cleanup() {
        executorService.shutdown();
    }
}
