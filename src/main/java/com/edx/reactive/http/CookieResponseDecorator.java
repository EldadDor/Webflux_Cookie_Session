package com.edx.reactive.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CookieResponseDecorator extends ServerHttpResponseDecorator {
    private static final Logger log = LogManager.getLogger(CookieResponseDecorator.class);
    private boolean cookiesWritten = false;
    private final List<ResponseCookie> pendingCookies = new ArrayList<>();

    public CookieResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return Mono.defer(() -> {
            if (!cookiesWritten && !pendingCookies.isEmpty()) {
                pendingCookies.forEach(getDelegate()::addCookie);
                cookiesWritten = true;
            }
            return getDelegate().writeWith(body);
        });
    }

    @Override
    public void addCookie(ResponseCookie cookie) {
        if (!isCommitted() && !cookiesWritten) {
            pendingCookies.add(cookie);
        }
    }

    private void writePendingCookies() {
        if (!cookiesWritten && !pendingCookies.isEmpty()) {
            pendingCookies.forEach(getDelegate()::addCookie);
            cookiesWritten = true;
        }
    }

    public boolean isCookiesWritten() {
        return cookiesWritten;
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {
        log.info("Registering before commit action");
        getDelegate().beforeCommit(() -> {
            return Mono.fromRunnable(() -> {
                if (!cookiesWritten && !pendingCookies.isEmpty()) {
                    pendingCookies.forEach(getDelegate()::addCookie);
                    cookiesWritten = true;
                }
            }).then(action.get());
        });
    }


    @Override
    public boolean setStatusCode(HttpStatusCode status) {
        return getDelegate().setStatusCode(status);
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return getDelegate().getStatusCode();
    }

    @Override
    public MultiValueMap<String, ResponseCookie> getCookies() {
        return getDelegate().getCookies();
    }


    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return getDelegate().writeAndFlushWith(body);
    }

    @Override
    public Mono<Void> setComplete() {
        return getDelegate().setComplete();
    }
}
