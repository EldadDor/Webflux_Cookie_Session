package com.edx.reactive.config;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.http.CglibProxyFactory;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;


@Component
public class CookieSessionBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext context;

    public CookieSessionBeanPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            CookieSession annotation = field.getAnnotation(CookieSession.class);
            if (annotation != null) {
                String cookieName = annotation.value().isEmpty() ? field.getName() : annotation.value();
                injectCookieData(bean, field, cookieName);
            }
        }
        return bean;
    }

    private void injectCookieData(Object bean, Field field, String cookieName) {
        field.setAccessible(true);
        Mono<CookieData> cookieDataMono = ReactiveRequestContextHolder.getExchange()
                .map(exchange -> (CookieData) exchange.getAttribute("cookieData"))
                .switchIfEmpty(Mono.fromSupplier(() -> createNewCookieData(field.getType())));

        cookieDataMono.subscribe(cookieData -> {
            try {
                field.set(bean, cookieData);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to inject cookie data", e);
            }
        });
    }


    private CookieData createNewCookieData(Class<?> type) {
        return (CookieData) CglibProxyFactory.createProxy(type);
    }
}
