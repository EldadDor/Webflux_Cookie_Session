package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;

public class CookieSessionRequestDecorator extends ServerHttpRequestDecorator {
    private final CookieData cookieData;

    public CookieSessionRequestDecorator(ServerHttpRequest delegate, CookieData cookieData) {
        super(delegate);
        this.cookieData = cookieData;
    }

    public CookieData getCookieData() {
        return cookieData;
    }
}
