package com.edx.reactive.config;

import com.edx.reactive.common.CookieDataWrapper;
import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.http.CookieSessionExchangeDecorator;
import com.edx.reactive.http.CookieSessionResponseDecorator;
import com.edx.reactive.utils.CookieDataProxyCreator;
import com.edx.reactive.utils.LazyLoadingCookieDataProxy;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.cglib.proxy.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


@Component
public class ExtendedCookieDataBeanPostProcessor implements BeanPostProcessor {


    ApplicationContext applicationContext;
    CookieDataProxyCreator proxyCreator;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            CookieSession annotation = field.getAnnotation(CookieSession.class);
            if (annotation != null) {
                injectCookieDataProxy(bean, field);
            }
        }
        return bean;
    }

    private void injectCookieDataProxy(Object bean, Field field) {
        field.setAccessible(true);
        try {
            Class<?> type = field.getType();
            if (CookieData.class.isAssignableFrom(type)) {
                Object proxy;
                if (type.isInterface()) {
                    proxy = Proxy.newProxyInstance(
                            type.getClassLoader(),
                            new Class<?>[]{type},
                            new CookieDataInvocationHandler()
                    );
                } else {
                    Enhancer enhancer = new Enhancer();
                    enhancer.setSuperclass(type);
                    enhancer.setCallback(new MethodInterceptor() {
                        @Override
                        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                            return new CookieDataInvocationHandler().invoke(obj, method, args);
                        }
                    });
                    proxy = enhancer.create();
                }
                field.set(bean, proxy);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error injecting cookie data proxy", e);
        }
    }

    private static class CookieDataInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return ReactiveRequestContextHolder.getExchange()
                    .flatMap(exchange -> {
                        if (exchange instanceof CookieSessionExchangeDecorator) {
                            CookieSessionExchangeDecorator decoratedExchange = (CookieSessionExchangeDecorator) exchange;
                            CookieData cookieData = decoratedExchange.getCookieData();
                            try {
                                Object result = method.invoke(cookieData, args);
                                if (method.getName().startsWith("set")) {
                                    ((CookieSessionResponseDecorator) decoratedExchange.getResponse()).setCookieDataChanged(true);
                                }
                                return Mono.justOrEmpty(result);
                            } catch (Exception e) {
                                return Mono.error(e);
                            }
                        }
                        return Mono.error(new IllegalStateException("CookieSessionExchangeDecorator not found in current exchange"));
                    })
                    .toFuture();
        }
    }



}