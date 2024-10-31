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
        CookieExchangeDecorator decoratedExchange = (CookieExchangeDecorator) exchange;

        return chain.filter(decoratedExchange)
                .then(decoratedExchange.getSession()
                        .flatMap(session -> handleResponse(decoratedExchange, session)));
    }


   /* @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        CookieExchangeDecorator decoratedExchange = (CookieExchangeDecorator) exchange;

        return chain.filter(decoratedExchange)
                .then(decoratedExchange.getSe()
                        .flatMap(session -> {
                            // Handle cookies after controller has processed request
                            CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                            log.info("Cookie data found: {}", cookieData != null);

                            if (cookieData != null) {
                                try {
                                    String jsonValue = objectMapper.writeValueAsString(cookieData);
                                    String encryptedValue = encryptionService.encryptAndCompress(jsonValue);

                                    ResponseCookie cookie = ResponseCookie.from(WebConstants.COOKIE_NAME, encryptedValue)
                                            .path("/")
                                            .build();

                                    decoratedExchange.getResponse().addCookie(cookie);
                                    log.info("Cookie queued for writing");
                                } catch (JsonProcessingException e) {
                                    log.error("Error serializing cookie data", e);
                                }
                            }
                            return Mono.empty();
                        })
                );
    }*/


    private Mono<Void> handleResponse(ServerWebExchange exchange, WebSession session) {
        return Mono.defer(() -> {
            CookieData cookieData = cookieDataManager.getCookieData(session.getId());
            log.info("Cookie data found: {}", cookieData != null);
            if (cookieData != null) {
                try {
                    String jsonValue = objectMapper.writeValueAsString(cookieData);
                    String encryptedValue = encryptionService.encryptAndCompress(jsonValue);

                    exchange.getResponse().beforeCommit(() -> {
                        ResponseCookie cookie = ResponseCookie.from(WebConstants.COOKIE_NAME, encryptedValue)
                                .path("/")
                                .build();
                        exchange.getResponse().addCookie(cookie);
                        return Mono.empty();
                    });
//
//                    ResponseCookie cookie = ResponseCookie.from(WebConstants.COOKIE_NAME, encryptedValue)
//                            .path("/")
//                            .build();

               /*     CookieResponseDecorator responseDecorator = (CookieResponseDecorator) exchange.getResponse();
                    responseDecorator.addCookie(cookie);
                    log.info("Cookie added to response");*/
                } catch (JsonProcessingException e) {
                    log.error("Error serializing cookie data", e);
                }
            }
            return Mono.empty();
        });
    }

    private Mono<Void> addCookieToResponse(ServerWebExchange exchange, WebSession session) {
        return Mono.defer(() -> {
            try {
                CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                if (cookieData != null) {
                    String jsonValue = objectMapper.writeValueAsString(cookieData);
                    String encryptedValue = encryptionService.encryptAndCompress(jsonValue);
                    log.info("Building cookie");
                    ResponseCookie cookie = ResponseCookie.from(WebConstants.COOKIE_NAME, encryptedValue)
                            .path("/")
                            .build();

                    CookieResponseDecorator responseDecorator = (CookieResponseDecorator) exchange.getResponse();
                    responseDecorator.addCookie(cookie);

                    log.info("Cookie added, isWritten={}", responseDecorator.isCookiesWritten());
                }
                return Mono.empty();
            } catch (JsonProcessingException e) {
                log.error("Error processing cookie", e);
                return Mono.error(e);
            }
        });
    }

    private boolean shouldUpdateCookie(CookieData cookieData) {
        return cookieData != null &&
                CglibProxyFactory.isProxy(cookieData) &&
                CglibProxyFactory.getInvocationHandler(cookieData) instanceof CglibProxyFactory.ModifyingMethodInterceptor &&
                ((CglibProxyFactory.ModifyingMethodInterceptor) CglibProxyFactory.getInvocationHandler(cookieData)).isModified();
    }
}
