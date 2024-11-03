package com.edx.reactive.utils;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public class ReactiveRequestContextHolder {
//    private static final ThreadLocal<ServerWebExchange> CONTEXT = new ThreadLocal<>();

   /* public static void setExchange(ServerWebExchange exchange) {
        CONTEXT.set(exchange);
    }
*/
  /*  public static ServerWebExchange getExchange() {
        return CONTEXT.get();
    }*/

//    public static void clear() {
//        CONTEXT.remove();
//    }

    private static final String CONTEXT_KEY = "SERVER_EXCHANGE_CONTEXT";

    public static Mono<ServerWebExchange> getExchange() {
        return Mono.deferContextual(ctx ->
                ctx.hasKey(CONTEXT_KEY)
                        ? Mono.just(ctx.get(CONTEXT_KEY))
                        : Mono.empty()
        );
    }

    // Utility method to add exchange to context
    public static Function<Context, Context> withExchange(ServerWebExchange exchange) {
        return context -> context.put(CONTEXT_KEY, exchange);
    }

/*
    // Add new reactive methods while maintaining ThreadLocal for compatibility
    public static Mono<ServerWebExchange> getExchangeReactive() {
        return Mono.deferContextual(ctx ->
                ctx.getOrEmpty("exchange")
                        .map(v -> (ServerWebExchange) v)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.justOrEmpty(CONTEXT.get()))
        );
    }*/
}


