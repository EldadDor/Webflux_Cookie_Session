package com.edx.reactive.utils;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class ReactiveRequestContextHolder {

    private static final Class<ServerWebExchange> CONTEXT_KEY = ServerWebExchange.class;

    public static Mono<ServerWebExchange> getExchange() {
        return Mono.deferContextual(contextView ->
                Mono.justOrEmpty(contextView.getOrEmpty(CONTEXT_KEY)));
    }

    public static Mono<Void> setExchange(ServerWebExchange exchange) {
        return Mono.deferContextual(contextView ->
                        Mono.just(exchange).contextWrite(context -> context.put(CONTEXT_KEY, exchange)))
                .then();
    }

    public static Mono<Context> getContext() {
        return Mono.deferContextual(contextView -> Mono.just(Context.of(contextView)));
    }
}

