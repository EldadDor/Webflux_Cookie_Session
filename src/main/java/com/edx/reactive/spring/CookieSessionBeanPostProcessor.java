package com.edx.reactive.spring;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.http.CookieDataFilter;
import com.edx.reactive.utils.CglibProxyFactory;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Field;

//@Component
public class CookieSessionBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LogManager.getLogger(CookieSessionBeanPostProcessor.class);

    private final CookieDataManager cookieDataManager;
    private final ApplicationContext applicationContext;

    public CookieSessionBeanPostProcessor(CookieDataManager cookieDataManager, ApplicationContext applicationContext) {
        this.cookieDataManager = cookieDataManager;
        this.applicationContext = applicationContext;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(CookieSession.class)) {
                injectProxiedCookieData(bean, field);
            }
        }
        return bean;
    }

    private void injectProxiedCookieData(Object bean, Field field) {
        field.setAccessible(true);
        ReactiveRequestContextHolder.getExchange()
                .flatMap(ServerWebExchange::getSession)
                .map(session -> {
                    CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                    if (cookieData == null) {
                        cookieData = createDefaultCookieData(field.getType());
                        if (cookieData != null) {
                            cookieDataManager.setCookieData(session.getId(), cookieData);
                        } else {
                            log.warn("Unable to create default CookieData for type: {}", field.getType());
                            return null;
                        }
                    }
                    CookieData proxiedData = CglibProxyFactory.createProxy(cookieData.getClass());
                    try {
                        field.set(bean, proxiedData);
                    } catch (IllegalAccessException e) {
                        log.error("Error injecting proxied cookie data", e);
                    }
                    return proxiedData;
                })
                .subscribe();
    }


    private CookieData createDefaultCookieData(Class<?> type) {
        // Create and return a default CookieData object based on the type
        return (CookieData) applicationContext.getBean(type);
    }


}