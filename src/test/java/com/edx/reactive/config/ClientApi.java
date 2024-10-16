package com.edx.reactive.config;

import com.edx.reactive.common.Client;
import com.edx.reactive.common.Portfolio;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface ClientApi {
    @GetExchange("/api/client")
    Client getClient();

    @PostExchange("/api/client")
    Client updateClient(Client client);

    @PostExchange("/api/client/portfolio")
    Client updatePortfolio(Portfolio portfolio);

    @PutExchange("/api/client")
    Client replaceClient(Client client);
}
