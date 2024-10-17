package com.edx.reactive;

import com.edx.reactive.common.Order;
import com.edx.reactive.common.OrderItem;
import com.edx.reactive.utils.CompressionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.edx.reactive.common.WebConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderControllerIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .responseTimeout(Duration.ofMinutes(5)) // Increase timeout to 30 seconds
                .build();
    }

    @Test
    void testOrderFlowWithCookieSession() throws Exception {
        // Scenario 1: Initial request with no cookie
        WebTestClient.ResponseSpec initialResponse = webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isOk();

        String initialOrderString = initialResponse.returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertTrue(initialOrderString.contains("Order: null, Items: 0, Total: 0.00"));

        String cookie = initialResponse.returnResult(String.class)
                .getResponseHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        assertNotNull(cookie);
        assertTrue(cookie.startsWith(COOKIE_NAME + "="));

        // Add an item to the order
        OrderItem newItem = new OrderItem("Test Item", 2, 10.0);
        WebTestClient.ResponseSpec addItemResponse = webTestClient.post().uri("/api/orders/add")
                .cookie(COOKIE_NAME, cookie.split("=")[1].split(";")[0])
                .body(Mono.just(newItem), OrderItem.class)
                .exchange()
                .expectStatus().isOk();

        String addItemResponseBody = addItemResponse.returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertTrue(addItemResponseBody.contains("Item added: Test Item, Quantity: 2, Price: 10.0"));

        // Scenario 2: Subsequent request with cookie
        WebTestClient.ResponseSpec subsequentResponse = webTestClient.get().uri("/api/orders")
                .cookie(COOKIE_NAME, cookie.split("=")[1].split(";")[0])
                .exchange()
                .expectStatus().isOk();

        String subsequentOrderString = subsequentResponse.returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertTrue(subsequentOrderString.contains("Order: "));
        assertTrue(subsequentOrderString.contains("Items: 1"));
        assertTrue(subsequentOrderString.contains("Total: 20.00"));

        // Scenario 3: Modify order and check persistence
        OrderItem secondItem = new OrderItem("Second Item", 1, 15.0);
        webTestClient.post().uri("/api/orders/add")
                .cookie(COOKIE_NAME, cookie.split("=")[1].split(";")[0])
                .body(Mono.just(secondItem), OrderItem.class)
                .exchange()
                .expectStatus().isOk();

        WebTestClient.ResponseSpec finalResponse = webTestClient.get().uri("/api/orders")
                .cookie(COOKIE_NAME, cookie.split("=")[1].split(";")[0])
                .exchange()
                .expectStatus().isOk();

        String finalOrderString = finalResponse.returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertTrue(finalOrderString.contains("Order: "));
        assertTrue(finalOrderString.contains("Items: 2"));
        assertTrue(finalOrderString.contains("Total: 35.00"));

        // Verify cookie content
        String finalCookie = finalResponse.returnResult(String.class)
                .getResponseHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        assertNotNull(finalCookie);
        String decodedCookie = CompressionUtils.decompressBase64(finalCookie.split("=")[1].split(";")[0]);
        Order decodedOrder = objectMapper.readValue(decodedCookie, Order.class);

        assertEquals(2, decodedOrder.getItems().size());
        assertEquals(35.00, decodedOrder.getTotal());
    }
}
