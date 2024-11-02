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
import org.springframework.cglib.proxy.MethodInterceptor;
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

//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 2)
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
        CookieExchangeDecorator decoratedExchange = (CookieExchangeDecorator) exchange;
        String path = exchange.getRequest().getURI().getPath();
        if (isSwaggerOrSpringDocUrl(path)) {
            return chain.filter(decoratedExchange);
        }

        return decoratedExchange.getSession()
                .flatMap(session -> {
                    HttpCookie cookie = decoratedExchange.getRequest().getCookies().getFirst(WebConstants.COOKIE_NAME);
                    if (cookie == null) {
                        if (!cookieDataManager.containsSession(session.getId())) {
                            cookieDataManager.createEmptySession(session.getId());
                        }
                        return chain.filter(decoratedExchange).log();
                    }
                    return processCookie(cookie, session.getId())
                            .flatMap(cookieData -> {
                                cookieDataManager.setCookieData(session.getId(), cookieData);
                                return chain.filter(decoratedExchange).log();
                            });
                });
    }


    private boolean isSwaggerOrSpringDocUrl(String path) {
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars/");
    }

    private Mono<CookieData> processCookie(HttpCookie cookie, String sessionId) {
        return Mono.fromCallable(() -> {
            String decryptedValue = encryptionService.decompressAndDecrypt(cookie.getValue());
            return objectMapper.readValue(decryptedValue, CookieData.class);
        }).onErrorResume(e -> {
            log.error("Error processing cookie", e);
            return Mono.just(createDefaultCookieData());
        });
    }


    private CookieData createDefaultCookieData() {
        // Create and return a default CookieData object based on the type
        return applicationContext.getBean(DefaultCookieData.class);
    }

}
