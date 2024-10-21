package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.utils.CglibProxyFactory;
import com.edx.reactive.utils.CookieDataInvocationHandler;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.CookieEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class CookieDataFilter implements WebFilter {

    private static final String COOKIE_NAME = "session_cookie";

    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;
    private final CookieDataManager cookieDataManager;

    @Autowired
    public CookieDataFilter(ObjectMapper objectMapper, CookieEncryptionService encryptionService, CookieDataManager cookieDataManager) {
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
        this.cookieDataManager = cookieDataManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getSession()
                .flatMap(session -> {
                    HttpCookie cookie = exchange.getRequest().getCookies().getFirst(COOKIE_NAME);

                    if (cookie == null) {
                        return chain.filter(exchange);
                    }

                    return processCookie(cookie, exchange)
                            .flatMap(cookieData -> {
                                cookieDataManager.setCookieData(session.getId(), cookieData);
                                return chain.filter(exchange);
                            });
                })
                .then(Mono.defer(() -> handleResponse(exchange)));
    }

    private Mono<CookieData> processCookie(HttpCookie cookie, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            String decryptedValue = encryptionService.decrypt(cookie.getValue());
            CookieData cookieData = objectMapper.readValue(decryptedValue, CookieData.class);
            return cookieData;
        }).onErrorResume(e -> {
            // Handle decryption or deserialization errors
            return Mono.just(new CookieData() {
                @Override
                public String name() {
                    return "defaultCookieData";
                }
            });
        });
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                    if (cookieData != null) {
                        CookieDataInvocationHandler invocationHandler = (CookieDataInvocationHandler) CglibProxyFactory.getInvocationHandler(cookieData);

                        if (invocationHandler.isModified()) {
                            try {
                                String jsonValue = objectMapper.writeValueAsString(cookieData);
                                String encryptedValue = encryptionService.encrypt(jsonValue);
                                ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, encryptedValue)
                                        .path("/")
                                        .build();
                                exchange.getResponse().addCookie(cookie);
                            } catch (JsonProcessingException e) {
                                // Handle exception
                            }
                        }
                    }
                    return Mono.empty();
                });
    }
}
