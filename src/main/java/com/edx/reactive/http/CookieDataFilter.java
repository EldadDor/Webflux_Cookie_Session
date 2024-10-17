package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.utils.CookieDataInvocationHandler;
import com.edx.reactive.utils.CookieEncryptionService;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.cglib.proxy.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Method;


//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CookieDataFilter implements WebFilter {

    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;

    public CookieDataFilter(ObjectMapper objectMapper, CookieEncryptionService encryptionService) {
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveRequestContextHolder.setExchange(exchange)
                .flatMap(context -> chain.filter(new CookieDataExchangeDecorator(exchange, objectMapper, encryptionService)));
    }
}

class CookieDataExchangeDecorator extends ServerWebExchangeDecorator {
    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;
    private final CookieDataRequestDecorator requestDecorator;
    private final CookieDataResponseDecorator responseDecorator;

    CookieDataExchangeDecorator(ServerWebExchange delegate, ObjectMapper objectMapper, CookieEncryptionService encryptionService) {
        super(delegate);
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
        this.requestDecorator = new CookieDataRequestDecorator(delegate.getRequest(), delegate, objectMapper, encryptionService);
        this.responseDecorator = new CookieDataResponseDecorator(delegate.getResponse(), delegate, objectMapper, encryptionService);
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}

class CookieDataRequestDecorator extends ServerHttpRequestDecorator {
    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;

    private final ServerWebExchange exchange;

    CookieDataRequestDecorator(ServerHttpRequest delegate, ServerWebExchange exchange, ObjectMapper objectMapper, CookieEncryptionService encryptionService) {
        super(delegate);
        this.exchange = exchange;
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
    }

    @Override
    public MultiValueMap<String, HttpCookie> getCookies() {
        MultiValueMap<String, HttpCookie> cookies = super.getCookies();
        HttpCookie cookie = cookies.getFirst("your_cookie_name");
        if (cookie != null) {
            processCookie(cookie);
        }
        return cookies;
    }

    private void processCookie(HttpCookie cookie) {
        try {
            String decryptedValue = encryptionService.decrypt(cookie.getValue());
            CookieData cookieData = objectMapper.readValue(decryptedValue, CookieData.class);
            CookieData proxiedData = CglibProxyFactory.createProxy(cookieData.getClass());
            objectMapper.readerForUpdating(proxiedData).readValue(decryptedValue);
            exchange.getAttributes().put("cookieData", proxiedData);
        } catch (IOException e) {
            // Handle exception
        }
    }
}

class CookieDataResponseDecorator extends ServerHttpResponseDecorator {
    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;
    private final ServerWebExchange exchange;

    CookieDataResponseDecorator(ServerHttpResponse delegate, ServerWebExchange exchange, ObjectMapper objectMapper, CookieEncryptionService encryptionService) {
        super(delegate);
        this.exchange = exchange;
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(body).doOnSuccess(v -> writeCookie());
    }

    private void writeCookie() {
        CookieData cookieData = (CookieData) exchange.getAttributes().get("cookieData");
        if (cookieData != null && cookieData instanceof Factory) {
            Callback callback = ((Factory) cookieData).getCallback(0);
            if (callback instanceof CglibProxyFactory.ModifyingMethodInterceptor) {
                CglibProxyFactory.ModifyingMethodInterceptor interceptor = (CglibProxyFactory.ModifyingMethodInterceptor) callback;
                if (interceptor.isModified()) {
                    try {
                        String jsonValue = objectMapper.writeValueAsString(cookieData);
                        String encryptedValue = encryptionService.encrypt(jsonValue);
                        ResponseCookie cookie = ResponseCookie.from("your_cookie_name", encryptedValue)
                                .path("/")
                                .build();
                        addCookie(cookie);
                    } catch (JsonProcessingException e) {
                        // Handle exception
                    }
                }
            }
        }
    }


}
