package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.DefaultCookieData;
import com.edx.reactive.common.WebConstants;
import com.edx.reactive.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class CookieDataFilter implements WebFilter {

    private static final Logger log = LogManager.getLogger(CookieDataFilter.class);

    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;
    private final CookieDataManager cookieDataManager;
    private final ApplicationContext applicationContext;

    @Autowired
    public CookieDataFilter(ObjectMapper objectMapper, CookieEncryptionService encryptionService, CookieDataManager cookieDataManager, ApplicationContext applicationContext) {
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
        this.cookieDataManager = cookieDataManager;
        this.applicationContext = applicationContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isSwaggerOrSpringDocUrl(path)) {
            return chain.filter(exchange);
        }
        return exchange.getSession()
                        .flatMap(session -> {
                            HttpCookie cookie = exchange.getRequest().getCookies().getFirst(WebConstants.COOKIE_NAME);
                            if (cookie == null) {
                                if (!cookieDataManager.containsSession(session.getId())) {
                                    cookieDataManager.createEmptySession(session.getId());
                                }
                                return chain.filter(exchange);
                            }
                            return processCookie(cookie, session.getId())
                                    .flatMap(cookieData -> {
                                        cookieDataManager.setCookieData(session.getId(), cookieData);
                                        return chain.filter(exchange);
                                    });
                        })
                .then(Mono.defer(() -> handleResponse(exchange)));
    }

    private boolean isSwaggerOrSpringDocUrl(String path) {
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars/");
    }

    private Mono<CookieData> processCookie(HttpCookie cookie, String sessionId) {
        return Mono.fromCallable(() -> {
            String decryptedValue = encryptionService.decrypt(cookie.getValue());
            return objectMapper.readValue(decryptedValue, CookieData.class);
        }).onErrorResume(e -> {
            log.error("Error processing cookie", e);
            return Mono.just(createDefaultCookieData());
        });
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                    if (cookieData != null && CglibProxyFactory.isProxy(cookieData)) {
                        CookieDataInvocationHandler invocationHandler = (CookieDataInvocationHandler) CglibProxyFactory.getInvocationHandler(cookieData);
                        if (invocationHandler.isModified()) {
                            try {
                                String jsonValue = objectMapper.writeValueAsString(cookieData);
                                String encryptedValue = encryptionService.encrypt(jsonValue);
                                ResponseCookie cookie = ResponseCookie.from(WebConstants.COOKIE_NAME, encryptedValue)
                                        .path("/")
                                        .build();
                                exchange.getResponse().addCookie(cookie);
                            } catch (JsonProcessingException e) {
                                log.error("Error serializing cookie data", e);
                            }
                        }
                    }
                    return Mono.empty();
                });
    }

    private CookieData createDefaultCookieData() {
        // Create and return a default CookieData object based on the type
        return applicationContext.getBean(DefaultCookieData.class);
    }

}
