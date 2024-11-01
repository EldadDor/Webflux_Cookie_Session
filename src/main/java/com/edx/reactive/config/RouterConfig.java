package com.edx.reactive.config;

import com.edx.reactive.http.CookieHandlerFilterFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

//@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> apiRoutes(CookieHandlerFilterFunction cookieFilter) {
        return RouterFunctions.route()
                .GET("/**", request -> ServerResponse.ok().build())
                .POST("/**", request -> ServerResponse.ok().build())
                .filter(cookieFilter)
                .build();
    }



    @Bean
    public RouterFunction<ServerResponse> swaggerRoutes() {
        return RouterFunctions.route()
                .path("/swagger-ui", builder ->
                        builder.GET("/**", request -> ServerResponse.ok().build()))
                .build();
    }
}
