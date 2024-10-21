package com.edx.reactive.spring;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class CookieSessionBeanPostProcessor implements BeanPostProcessor {



    private final CookieDataManager cookieDataManager;

    public CookieSessionBeanPostProcessor(CookieDataManager cookieDataManager) {
        this.cookieDataManager = cookieDataManager;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return ReactiveRequestContextHolder.getExchange()
                .flatMap(exchange -> exchange.getSession())
                .map(session -> {
                    CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                    if (cookieData != null) {
                        injectProxiedCookieData(bean, cookieData);
                    }
                    return bean;
                })
                .block();
    }


/*
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(CookieSession.class)) {
                injectProxiedCookieData(bean, field);
            }
        }
        return bean;
    }*/

    private void injectProxiedCookieData(Object bean, CookieData cookieData) {
        // ... existing code ...
    }
}