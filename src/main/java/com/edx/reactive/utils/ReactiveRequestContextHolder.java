package com.edx.reactive.utils;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class ReactiveRequestContextHolder {
    public static final Class<ServerWebExchange> CONTEXT_KEY = ServerWebExchange.class;

    public static Mono<ServerWebExchange> getExchange() {
        return Mono.deferContextual(contextView ->
                contextView.<ServerWebExchange>getOrEmpty(CONTEXT_KEY)
                        .map(Mono::just)
                        .orElse(Mono.empty())
        );
    }

    public static Mono<Void> setExchange(ServerWebExchange exchange) {
        return Mono.deferContextual(contextView ->
                Mono.just(Context.of(CONTEXT_KEY, exchange).putAll(contextView))
        ).contextWrite(context -> context.put(CONTEXT_KEY, exchange)).then();
    }
}
