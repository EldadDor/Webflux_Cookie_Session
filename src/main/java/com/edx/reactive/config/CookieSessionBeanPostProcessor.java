package com.edx.reactive.config;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieDataWrapper;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.http.CookieSessionFilter;
import com.edx.reactive.utils.CookieDataProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Field;

//@Component
public class CookieSessionBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CookieDataProxyCreator proxyCreator;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            CookieSession annotation = field.getAnnotation(CookieSession.class);
            if (annotation != null) {
                injectCookieSessionData(bean, field);
            }
        }
        return bean;
    }

    private void injectCookieSessionData(Object bean, Field field) {
        field.setAccessible(true);
        try {
            Object cookieData = applicationContext.getBean(ServerWebExchange.class)
                    .getAttribute(CookieSessionFilter.COOKIE_SESSION_DATA);

            if (cookieData == null) {
                Class<?> fieldType = field.getType();
                if (CookieData.class.isAssignableFrom(fieldType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends CookieData> cookieDataType = (Class<? extends CookieData>) fieldType;
                    cookieData = createNewCookieDataProxy(cookieDataType);
                } else {
                    throw new IllegalArgumentException("Field type must implement CookieData interface");
                }
            }

            field.set(bean, cookieData);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject cookie session data", e);
        }
    }

    private <T extends CookieData> T createNewCookieDataProxy(Class<T> type) {
        CookieDataWrapper<T> wrapper = new CookieDataWrapper<>(null);
        wrapper.setChanged(true);
        return proxyCreator.createProxy(wrapper, type);
    }

 /*   private Object createNewCookieDataProxy(Class<?> type) {
        if (CookieData.class.isAssignableFrom(type)) {
            CookieDataWrapper<?> wrapper = new CookieDataWrapper<>(null);
            wrapper.setChanged(true);
            return proxyCreator.createProxy(wrapper, type);
        }
        throw new IllegalArgumentException("Field type must implement CookieData interface");
    }*/
}
