package com.edx.reactive.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class CookieExchangeDecorator extends ServerWebExchangeDecorator {
    private static final Logger log = LogManager.getLogger(CookieExchangeDecorator.class);

    private final ServerHttpResponse responseDecorator;

    public CookieExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
        ServerHttpResponse originalResponse = delegate.getResponse();
        this.responseDecorator = new CookieResponseDecorator(originalResponse) {
            @Override
            public boolean isCommitted() {
                boolean committed = super.isCommitted();
                if (committed) {
                    log.info("Response committed at: ", new Exception("Stack trace"));
                }
                return committed;
            }

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                log.info("Writing response body");
                return super.writeWith(body)
                        .doOnSubscribe(s -> log.info("Starting to write response"))
                        .doOnSuccess(v -> log.info("Finished writing response"))
                        .doOnError(e -> log.error("Error writing response", e));
            }

            @Override
            public void beforeCommit(Supplier<? extends Mono<Void>> action) {
                log.info("Before commit action registered");
                super.beforeCommit(() -> {
                    log.info("Executing before commit action");
                    return action.get()
                            .doOnSuccess(v -> log.info("Before commit action completed"))
                            .doOnError(e -> log.error("Error in before commit action", e));
                });
            }
        };
    }



    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}
