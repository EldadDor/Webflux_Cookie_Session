package com.edx.reactive.http;

import org.apache.logging.log4j.core.config.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


//@Component
//@Order(Ordered.LOWEST_PRECEDENCE)
public class CookieWebFilter implements WebFilter {

    @Autowired
    private CookieHandlerFilterFunction cookieHandlerFilter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerRequest serverRequest = ServerRequest.create(
                exchange,
                HandlerStrategies.withDefaults().messageReaders()
        );

        // Intercept before the chain proceeds
        return exchange.getSession()
                .flatMap(session -> {
                    // Store the original response status and headers
                    return chain.filter(exchange)
                            .then(Mono.defer(() -> {
                                // After the chain completes, but before response is written
                                return cookieHandlerFilter.filter(
                                        serverRequest,
                                        req -> ServerResponse.status(exchange.getResponse().getStatusCode())
                                                .headers(headers -> headers.addAll(exchange.getResponse().getHeaders()))
                                                .build()
                                ).flatMap(modifiedResponse -> {
                                    // Copy cookies from modified response to exchange response
                                    modifiedResponse.cookies().forEach((name, cookies) ->
                                            cookies.forEach(cookie ->
                                                    exchange.getResponse().addCookie(cookie)));
                                    return Mono.empty();
                                });
                            }));
                });
    }
}
