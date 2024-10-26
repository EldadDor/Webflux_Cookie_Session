package com.edx.reactive.http;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.concurrent.Flow;
import java.util.function.Supplier;


public class CookieResponseDecorator implements ServerHttpResponse {
    private final ServerHttpResponse delegate;
    private boolean cookiesWritten = false;

    public CookieResponseDecorator(ServerHttpResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addCookie(ResponseCookie cookie) {
        if (!isCommitted()) {
            delegate.addCookie(cookie);
            cookiesWritten = true;
        }
    }

    @Override
    public boolean isCommitted() {
        return delegate.isCommitted();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public DataBufferFactory bufferFactory() {
        return delegate.bufferFactory();
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {
        delegate.beforeCommit(action);
    }

    @Override
    public boolean setStatusCode(HttpStatusCode status) {
        return delegate.setStatusCode(status);
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return delegate.getStatusCode();
    }

    @Override
    public MultiValueMap<String, ResponseCookie> getCookies() {
        return delegate.getCookies();
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return delegate.writeWith(body);
    }


    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return delegate.writeAndFlushWith(body);
    }

    @Override
    public Mono<Void> setComplete() {
        return delegate.setComplete();
    }
}
