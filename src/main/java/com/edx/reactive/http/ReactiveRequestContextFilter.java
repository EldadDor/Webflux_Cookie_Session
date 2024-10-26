package com.edx.reactive.http;

import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ReactiveRequestContextFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ReactiveRequestContextHolder.setExchange(exchange);
        return chain.filter(exchange)
                .doFinally(signalType -> ReactiveRequestContextHolder.clear());
    }
}