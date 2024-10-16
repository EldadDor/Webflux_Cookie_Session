package com.edx.reactive.http;

import com.edx.reactive.common.Client;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.common.Portfolio;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @CookieSession
    private Client client;

    @GetMapping
    public Mono<Client> getClient() {
        return Mono.justOrEmpty(client);
    }

    @PostMapping
    public Mono<Client> updateClient(@RequestBody Client newClient) {
        // Check if the injected client is different from the request body
        if (!newClient.getId().equals(client.getId())) {
            return Mono.error(new IllegalArgumentException("Client ID mismatch"));
        }

        // Update the injected client
        client.setFirstName(newClient.getFirstName());
        client.setLastName(newClient.getLastName());

        return Mono.just(client);
    }

    @PostMapping("/portfolio")
    public Mono<Client> updatePortfolio(@RequestBody Portfolio newPortfolio) {
        client.setPortfolio(newPortfolio);
        return Mono.just(client);
    }

    @PutMapping
    public Mono<Client> replaceClient(@RequestBody Client newClient) {
        // Replace the entire client object
        this.client = newClient;
        return Mono.just(client);
    }
}