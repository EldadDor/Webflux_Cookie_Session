package com.edx.reactive.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggerWebFilter implements WebFilter {
	private static final Logger LOGGER = LogManager.getLogger(LoggerWebFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		final long requestStartMillis = System.currentTimeMillis();
		final ServerHttpRequest request = exchange.getRequest();
		final URI uri = request.getURI();
		final HttpMethod httpMethod = request.getMethod();
		final String query = Optional.of(request).map(ServerHttpRequest::getQueryParams)
				.map(MultiValueMap::toSingleValueMap)
				.map(this::mapKeyValueToFormattedString)
				.orElse("");

		final String headers = Optional.of(request).map(HttpMessage::getHeaders)
				.map(HttpHeaders::toSingleValueMap)
				.map(this::mapKeyValueToFormattedString)
				.orElse("");
		boolean logRequest = !uri.getPath().contains("/actuator/");

		final String requestString = String.format("uri=[%s], method=[%s], headers=-[%s], query=[%s]", uri, httpMethod, headers, query);


		if (logRequest)
			LOGGER.info("request income={}", requestString);
		{
			return chain.filter(exchange)
					.doOnError(throwable -> System.out.println("throwable = " + throwable))
					.doFinally(signalType -> {
						if (logRequest) {
							 HttpStatusCode httpStatus = Optional.of(exchange).map(ServerWebExchange::getResponse).map(ServerHttpResponse::getStatusCode).orElse(null);
						LOGGER.info("request finished in  lapTime={} ms status={}, {}", System.currentTimeMillis() - requestStartMillis,
								httpStatus, requestString);
						}
					});
		}
	}


	private String mapKeyValueToFormattedString(Map<String, String> map) {
		return map.keySet().stream()
				.map(key -> key + "=" + map.get(key))
				.collect(Collectors.joining(",", "{", "}"));
	}
}
