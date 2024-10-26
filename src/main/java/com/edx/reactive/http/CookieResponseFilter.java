package com.edx.reactive.http;


import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.WebConstants;
import com.edx.reactive.utils.CglibProxyFactory;
import com.edx.reactive.utils.CookieDataInvocationHandler;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.CookieEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class CookieResponseFilter implements WebFilter {

    private static final Logger log = LogManager.getLogger(CookieResponseFilter.class);

    @Autowired
    private CookieDataManager cookieDataManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CookieEncryptionService encryptionService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getSession()
                .flatMap(session -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .response(new CookieResponseDecorator(exchange.getResponse()))
                            .build();
                    return chain.filter(mutatedExchange)
                            .then(Mono.defer(() -> handleResponse(mutatedExchange, session)));
                });
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange, WebSession session) {
        CookieData cookieData = cookieDataManager.getCookieData(session.getId());
        if (shouldUpdateCookie(cookieData)) {
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
        return Mono.empty();
    }

    private boolean shouldUpdateCookie(CookieData cookieData) {
        return cookieData != null &&
                CglibProxyFactory.isProxy(cookieData) &&
                CglibProxyFactory.getInvocationHandler(cookieData) instanceof CglibProxyFactory.ModifyingMethodInterceptor &&
                ((CglibProxyFactory.ModifyingMethodInterceptor) CglibProxyFactory.getInvocationHandler(cookieData)).isModified();
    }
}

