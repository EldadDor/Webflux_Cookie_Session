package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

public class CookieSessionExchangeDecorator extends ServerWebExchangeDecorator {
    private final CookieData cookieData;

    public CookieSessionExchangeDecorator(ServerWebExchange delegate, CookieData cookieData) {
        super(delegate);
        this.cookieData = cookieData;
    }

    @Override
    public ServerHttpRequest getRequest() {
        return new CookieSessionRequestDecorator(getDelegate().getRequest(), cookieData);
    }

    @Override
    public ServerHttpResponse getResponse() {
        return new CookieSessionResponseDecorator(getDelegate().getResponse(), cookieData);
    }

    public CookieData getCookieData() {
        return cookieData;
    }
}
