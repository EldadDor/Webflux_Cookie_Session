package com.edx.reactive.config;

import com.edx.reactive.common.Client;
import com.edx.reactive.common.Portfolio;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

//@Configuration
public class TestClientConfig {


   /* @Bean
    public CookieDataProxyCreator cookieDataProxyCreator() {
        return new CookieDataProxyCreator();
    }*/

 /*   @Bean
    public ExtendedCookieDataBeanPostProcessor extendedCookieDataBeanPostProcessor(
            ApplicationContext applicationContext, CookieDataProxyCreator proxyCreator) {
        return new ExtendedCookieDataBeanPostProcessor(applicationContext, proxyCreator);
    }
*/
    @Bean
    public WebTestClient webTestClient() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Bean
    public ClientApi restClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor((HttpExchangeAdapter) RestClient.create("http://localhost:8080"))
                .build();

        return factory.createClient(ClientApi.class);
    }
}

