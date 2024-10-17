package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieDataWrapper;
import com.edx.reactive.common.DefaultCookieData;
import com.edx.reactive.utils.CookieDataProxyCreator;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CookieSessionFilter implements WebFilter {

    private static final String COOKIE_NAME = "x-cookie-name";
    public static final String COOKIE_SESSION_DATA = "COOKIE_SESSION_DATA";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CookieDataProxyCreator proxyCreator;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(COOKIE_NAME))
                .map(HttpCookie::getValue)
                .map(this::decryptAndDecompress)
                .map(this::deserializeCookieData)
                .defaultIfEmpty(createDefaultCookieData())
                .map(cookieData -> new CookieSessionExchangeDecorator(exchange, cookieData))
                .flatMap(decoratedExchange ->
                        ReactiveRequestContextHolder.setExchange(decoratedExchange)
                                .then(chain.filter(decoratedExchange))
                                .then(Mono.fromRunnable(() -> handleResponse(decoratedExchange)))
                )
                .contextWrite(ctx -> ctx.put(ReactiveRequestContextHolder.CONTEXT_KEY, exchange))
                .then();
    }

    private CookieData createDefaultCookieData() {
        // Create and return a default CookieData object
        // This method should be implemented based on your CookieData interface
        return new DefaultCookieData(); // Implement this class as needed
    }

    private void handleResponse(CookieSessionExchangeDecorator exchange) {
        CookieSessionResponseDecorator response = (CookieSessionResponseDecorator) exchange.getResponse();
        if (response.isCookieDataChanged()) {
            String serializedData = serializeCookieData(response.getCookieData());
            String compressedEncryptedData = compressAndEncrypt(serializedData);
            response.addCookie(ResponseCookie.from(COOKIE_NAME, compressedEncryptedData).build());
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends CookieData> T createProxyForCookieData(CookieData data) {
        CookieDataWrapper<T> wrapper = new CookieDataWrapper<>((T) data);
        return proxyCreator.createProxy(wrapper, (Class<T>) data.getClass());
    }


    private String decryptAndDecompress(String value) {
        // Implement decryption and decompression
        return value;
    }

    private CookieData deserializeCookieData(String json) {
        try {
            return objectMapper.readValue(json, CookieData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cookie data", e);
        }
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange) {
        Object cookieData = exchange.getAttribute(COOKIE_SESSION_DATA);
        if (cookieData instanceof CookieDataWrapper && ((CookieDataWrapper<?>) cookieData).isChanged()) {
            String serializedData = serializeCookieData(((CookieDataWrapper<?>) cookieData).getData());
            String compressedEncryptedData = compressAndEncrypt(serializedData);
            exchange.getResponse().addCookie(ResponseCookie.from(COOKIE_NAME, compressedEncryptedData).build());
        }
        return Mono.empty();
    }

    private String serializeCookieData(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize cookie data", e);
        }
    }

    private String compressAndEncrypt(String data) {
        // Implement compression and encryption
        return data;
    }
}
