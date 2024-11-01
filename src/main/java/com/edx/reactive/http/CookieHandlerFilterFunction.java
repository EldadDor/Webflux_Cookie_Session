package com.edx.reactive.http;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.WebConstants;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.CookieEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Component
public class CookieHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LogManager.getLogger(CookieHandlerFilterFunction.class);
    @Autowired
    private CookieDataManager cookieDataManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CookieEncryptionService encryptionService;

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                .flatMap(response -> request.session()
                        .flatMap(session -> addCookieToResponse(session, response))
                        .defaultIfEmpty(response));
    }

    private Mono<ServerResponse> addCookieToResponse(WebSession session, ServerResponse originalResponse) {
        return Mono.defer(() -> {
            try {
                CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                if (cookieData == null) {
                    return Mono.just(originalResponse);
                }
                String jsonValue = objectMapper.writeValueAsString(cookieData);
                String encryptedValue = encryptionService.encryptAndCompress(jsonValue);
//                String encryptedValue = "TESTCookieValue";

                ResponseCookie cookie = ResponseCookie.from(WebConstants.COOKIE_NAME, encryptedValue)
                        .path("/")
                        .build();

                // Create a new response with the additional cookie using BodyBuilder
                Mono<ServerResponse> build = ServerResponse
                        .status(originalResponse.statusCode())
                        .headers(headers -> headers.addAll(originalResponse.headers()))
                        .cookies(cookies -> {
                            cookies.addAll(originalResponse.cookies());
                            cookies.add(WebConstants.COOKIE_NAME, cookie);
                        })
                        .build();
                return build;

            } catch (JsonProcessingException e) {
                log.error("Error processing cookie", e);
                return Mono.error(e);
            }
        });
    }
}

