package com.edx.reactive.utils;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class ReactiveRequestContextHolder {
    private static final ThreadLocal<ServerWebExchange> CONTEXT = new ThreadLocal<>();

    public static void setExchange(ServerWebExchange exchange) {
        CONTEXT.set(exchange);
    }

    public static ServerWebExchange getExchange() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    // Add new reactive methods while maintaining ThreadLocal for compatibility
    public static Mono<ServerWebExchange> getExchangeReactive() {
        return Mono.deferContextual(ctx ->
                ctx.getOrEmpty("exchange")
                        .map(v -> (ServerWebExchange) v)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.justOrEmpty(CONTEXT.get()))
        );
    }
}


