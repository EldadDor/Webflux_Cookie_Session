package com.edx.reactive.config;

import com.edx.reactive.http.CookieHandlerFilterFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

public class RouterConfig   {

    @Autowired
    private CookieHandlerFilterFunction cookieFilter;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
                .GET("/**", this::forwardToController)
                .POST("/**", this::forwardToController)
                .filter(cookieFilter)
                .build();
    }

    private Mono<ServerResponse> forwardToController(ServerRequest request) {
        return ServerResponse.status(HttpStatus.OK).build();
    }
}
