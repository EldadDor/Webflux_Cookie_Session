package com.edx.reactive.config;

import com.edx.reactive.http.CookieHandlerFilterFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

//@Configuration
public class WebFluxConfig {

   /* @Autowired
    private CookieHandlerFilterFunction cookieHandlerFilter;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
                .path("/api", builder -> builder
                        .filter(cookieHandlerFilter))
                .build();
    }


    private Mono<ServerResponse> addCookieToResponse(ServerRequest request, ServerResponse response) {
        return cookieHandlerFilter.filter(request, req -> Mono.just(response));
    }

    private ServerRequest addRequestContext(ServerRequest request) {
        // Add any request context if needed
        return request;
    }
*/

    @Autowired
    private CookieHandlerFilterFunction cookieHandlerFilter;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
                .GET("/vehicles/{id}", this::forwardRequest)
                .GET("/vehicles/color/{color}", this::forwardRequest)
                .GET("/vehicles/type/{type}", this::forwardRequest)
                .POST("/vehicles", this::forwardRequest)
                .filter(cookieHandlerFilter)
                .build();
    }

    private Mono<ServerResponse> forwardRequest(ServerRequest request) {
        // This method forwards the request to allow the RestController to handle it
        return ServerResponse.ok().build();
    }

}
