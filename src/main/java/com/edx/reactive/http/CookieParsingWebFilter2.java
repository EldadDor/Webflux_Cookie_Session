package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieDataWrapper;
import com.edx.reactive.utils.CompressionUtils;
import com.edx.reactive.utils.CookieDataProxyCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static com.edx.reactive.common.WebConstants.COOKIE_NAME;
import static com.edx.reactive.common.WebConstants.COOKIE_SESSION_DATA;

@Component
@Order(1)
public class CookieParsingWebFilter2 implements WebFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CookieDataProxyCreator proxyCreator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(COOKIE_NAME))
                .map(HttpCookie::getValue)
                .map(CompressionUtils::decompressBase64)
                .defaultIfEmpty("{}")
                .flatMap(cookieJson -> {
                    CookieData cookieData = parseCookieData(cookieJson);
                    CookieDataWrapper<CookieData> wrapper = new CookieDataWrapper<>(cookieData);
                    CookieData proxy = proxyCreator.createProxy(wrapper, (Class<CookieData>) (cookieData != null ? cookieData.getClass() : CookieData.class));
                    exchange.getAttributes().put(COOKIE_SESSION_DATA, proxy);
                    return chain.filter(exchange);
                })
                .then(Mono.defer(() -> {
                    CookieData cookieData = exchange.getAttribute(COOKIE_SESSION_DATA);
                    if (cookieData instanceof CookieDataWrapper && ((CookieDataWrapper<?>) cookieData).isChanged()) {
                        String serializedData = serializeCookieData(((CookieDataWrapper<?>) cookieData).getData());
                        String compressedData = CompressionUtils.compressBase64(serializedData);
                        exchange.getResponse().addCookie(ResponseCookie.from(COOKIE_NAME, compressedData).build());
                    }
                    return Mono.empty();
                }));
    }

    private CookieData parseCookieData(String json) {
        try {
            return objectMapper.readValue(json, CookieData.class);
        } catch (Exception e) {
            // Return null if parsing fails or json is empty
            return null;
        }
    }

    private String serializeCookieData(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing cookie data", e);
        }
    }
}
