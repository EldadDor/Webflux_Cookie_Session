package com.edx.reactive;

import com.edx.reactive.common.Client;
import com.edx.reactive.common.Portfolio;
import com.edx.reactive.config.ClientApi;
import com.edx.reactive.config.TestClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({CookieInjectionApplication.class, TestClientConfig.class})
class ClientControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ClientApi restClient;

    @Test
    void testClientFlowWithBothClients() {
        // Test initial state with WebTestClient
        webTestClient.get().uri("/api/client")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class)
                .value(client -> assertNull(client.getId()));

        // Create a new client with RestClient
        Client newClient = new Client();
        newClient.setId("1");
        newClient.setFirstName("John");
        newClient.setLastName("Doe");

        Client createdClient = restClient.replaceClient(newClient);
        assertEquals("1", createdClient.getId());
        assertEquals("John", createdClient.getFirstName());
        assertEquals("Doe", createdClient.getLastName());

        // Verify client creation with WebTestClient
        webTestClient.get().uri("/api/client")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class)
                .value(client -> {
                    assertEquals("1", client.getId());
                    assertEquals("John", client.getFirstName());
                    assertEquals("Doe", client.getLastName());
                });

        // Update portfolio with WebTestClient
        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioId("P1");
        portfolio.setTotalValue(10000.0);
        portfolio.setStockSymbols(Arrays.asList("AAPL", "GOOGL"));
        portfolio.setNumberOfStocks(2);

        webTestClient.post().uri("/api/client/portfolio")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(portfolio), Portfolio.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class)
                .value(client -> {
                    assertNotNull(client.getPortfolio());
                    assertEquals("P1", client.getPortfolio().getPortfolioId());
                });

        // Verify portfolio update with RestClient
        Client updatedClient = restClient.getClient();
        assertNotNull(updatedClient.getPortfolio());
        assertEquals("P1", updatedClient.getPortfolio().getPortfolioId());
        assertEquals(10000.0, updatedClient.getPortfolio().getTotalValue());
        assertEquals(2, updatedClient.getPortfolio().getNumberOfStocks());

        // Update client with WebTestClient
        Client updatedClientInfo = new Client();
        updatedClientInfo.setId("1");
        updatedClientInfo.setFirstName("Jane");
        updatedClientInfo.setLastName("Doe");

        webTestClient.post().uri("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedClientInfo), Client.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class)
                .value(client -> {
                    assertEquals("1", client.getId());
                    assertEquals("Jane", client.getFirstName());
                    assertEquals("Doe", client.getLastName());
                });

        // Final verification with RestClient
        Client finalClient = restClient.getClient();
        assertEquals("1", finalClient.getId());
        assertEquals("Jane", finalClient.getFirstName());
        assertEquals("Doe", finalClient.getLastName());
        assertNotNull(finalClient.getPortfolio());
        assertEquals("P1", finalClient.getPortfolio().getPortfolioId());
        assertEquals(10000.0, finalClient.getPortfolio().getTotalValue());
        assertEquals(2, finalClient.getPortfolio().getNumberOfStocks());
    }
}
