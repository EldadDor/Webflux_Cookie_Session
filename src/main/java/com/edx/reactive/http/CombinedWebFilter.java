package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.DefaultCookieData;
import com.edx.reactive.common.WebConstants;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.CookieEncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class CombinedWebFilter implements WebFilter {

    private static final Logger log = LogManager.getLogger(CombinedWebFilter.class);

    private final ObjectMapper objectMapper;
    private final CookieEncryptionService encryptionService;
    private final CookieDataManager cookieDataManager;
    private final ApplicationContext applicationContext;

    public CombinedWebFilter(ObjectMapper objectMapper, CookieEncryptionService encryptionService, CookieDataManager cookieDataManager, ApplicationContext applicationContext) {
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
        this.cookieDataManager = cookieDataManager;
        this.applicationContext = applicationContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getSession()
                .flatMap(session -> {
                    HttpCookie cookie = exchange.getRequest().getCookies().getFirst(WebConstants.COOKIE_NAME);

                    if (cookie == null) {
                        if (!cookieDataManager.containsSession(session.getId())) {
                            cookieDataManager.createEmptySession(session.getId());
                        }
                        return processResponse(exchange, chain, session.getId());
                    }

                    return processCookie(cookie, session.getId())
                            .flatMap(cookieData -> {
                                cookieDataManager.setCookieData(session.getId(), cookieData);
                                return processResponse(exchange, chain, session.getId());
                            });
                });
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



    private Mono<Void> processResponse(ServerWebExchange exchange,
                                       WebFilterChain chain,
                                       String sessionId) {
        ServerHttpResponse response = exchange.getResponse();

        // Create response decorator for Mono responses
        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Since we know it's a Mono, we can use just one transformation
                return Mono.from(body)
                        .flatMap(dataBuffer -> {
                            // Add cookie before writing the response
                            CookieData cookieData = cookieDataManager.getCookieData(sessionId);
                            if (cookieData != null) {
                                ResponseCookie responseCookie = ResponseCookie.from(WebConstants.COOKIE_NAME,
                                                "TestCookieValue")
                                        .path("/")
                                        .maxAge(Duration.ofDays(1))
                                        .httpOnly(true)
                                        .secure(true)
                                        .build();

                                exchange.getResponse().getCookies().add(
                                        responseCookie.getName(),
                                        responseCookie
                                );
                            }

                            return super.writeWith(Mono.just(dataBuffer));
                        });
            }
        };

        // Create decorated exchange with our response decorator
        ServerWebExchange decoratedExchange = exchange.mutate()
                .response(responseDecorator)
                .build();

        return chain.filter(decoratedExchange).log();
    }


    private CookieData createDefaultCookieData() {
        // Create and return a default CookieData object based on the type
        return applicationContext.getBean(DefaultCookieData.class);
    }
}