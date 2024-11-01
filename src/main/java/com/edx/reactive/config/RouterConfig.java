package com.edx.reactive.config;

import com.edx.reactive.http.CookieHandlerFilterFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Configuration
public class RouterConfig {

    @Autowired
    private CookieHandlerFilterFunction cookieHandlerFilter;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
                .path("/vehicles", builder -> builder
                        // Match exact paths instead of using /**
                        .GET("", this::forwardToController)
                        .GET("/{id}", this::forwardToController)
                        .GET("/color/{color}", this::forwardToController)
                        .GET("/type/{type}", this::forwardToController)
                        .POST("", this::forwardToController))
                .filter(cookieHandlerFilter)
                .build();
    }

    private Mono<ServerResponse> forwardToController(ServerRequest request) {
        // Forward to the RestController
        return ServerResponse.ok().build();
    }
}
