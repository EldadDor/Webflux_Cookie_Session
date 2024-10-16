package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

public class CookieSessionResponseDecorator extends ServerHttpResponseDecorator {
    private final CookieData cookieData;
    private boolean cookieDataChanged = false;

    public CookieSessionResponseDecorator(ServerHttpResponse delegate, CookieData cookieData) {
        super(delegate);
        this.cookieData = cookieData;
    }

    public void setCookieDataChanged(boolean changed) {
        this.cookieDataChanged = changed;
    }

    public boolean isCookieDataChanged() {
        return cookieDataChanged;
    }

    public CookieData getCookieData() {
        return cookieData;
    }
}
