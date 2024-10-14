package com.edx.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.io.ByteArrayOutputStream;

import static com.edx.reactive.WebConstants.COOKIE_SESSION_DATA;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CookieParsingWebFilter2 implements WebFilter {
	public static final String COOKIE_DATA_KEY = "cookieData";
	public static final String COOKIE_NAME = "yourCookieName";

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(COOKIE_NAME))
				.map(HttpCookie::getValue)
				.map(CompressionUtils::decompressBase64)
				.defaultIfEmpty("{}")
				.flatMap(cookieJson -> {
					Object cookieData = parseCookieData(cookieJson);
					CookieDataWrapper<Object> wrapper = new CookieDataWrapper<>(cookieData);
					return chain.filter(exchange)
							.contextWrite(context -> context.put(COOKIE_SESSION_DATA, wrapper));
				})
				.then(Mono.deferContextual(context ->
						Mono.justOrEmpty(context.getOrEmpty(COOKIE_SESSION_DATA))
								.cast(CookieDataWrapper.class)
								.filter(CookieDataWrapper::isChanged)
								.map(wrapper -> wrapper.getData())
								.map(this::serializeCookieData)
								.map(CompressionUtils::compressBase64)
								.doOnNext(compressedData ->
										exchange.getResponse().addCookie(ResponseCookie.from(COOKIE_SESSION_DATA, compressedData).build())
								)
								.then()
				));
	}

	private Object parseCookieData(String json) {
		try {
			return objectMapper.readValue(json, Object.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error parsing cookie data", e);
		}
	}

	private String serializeCookieData(Object data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error serializing cookie data", e);
		}
	}
}
