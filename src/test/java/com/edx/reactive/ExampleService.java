package com.edx.reactive;

import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExampleService {
    public Mono<String> doSomething() {
        return ReactiveRequestContextHolder.getExchange()
                .map(exchange -> {
                    // Access exchange here
                    HttpCookie cookie = exchange.getRequest().getCookies()
                            .getFirst("someCookie");
                    return cookie != null ? cookie.getValue() : "default";
                });
    }
}