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
        ServerWebExchange decoratedExchange = new CookieExchangeDecorator(exchange);
        String path = exchange.getRequest().getURI().getPath();
       /* if (isSwaggerUrl(path)) {
            return chain.filter(decoratedExchange).log();
        }*/

        ReactiveRequestContextHolder.setExchange(decoratedExchange);
        return chain.filter(decoratedExchange)
                .contextWrite(ctx -> ctx.put("exchange", exchange)).log()
                .doFinally(signalType -> ReactiveRequestContextHolder.clear());
    }

    private boolean isSwaggerUrl(String path) {
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/");
    }



}