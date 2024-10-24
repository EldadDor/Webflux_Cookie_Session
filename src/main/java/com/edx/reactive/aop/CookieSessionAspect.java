package com.edx.reactive.aop;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.spring.CookieSessionBeanPostProcessor;
import com.edx.reactive.utils.CglibProxyFactory;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Aspect
@Component
public class CookieSessionAspect {

    private static final Logger log = LogManager.getLogger(CookieSessionAspect.class);

    private final CookieDataManager cookieDataManager;
    private final ApplicationContext applicationContext;


    public CookieSessionAspect(CookieDataManager cookieDataManager, ApplicationContext applicationContext) {
        this.cookieDataManager = cookieDataManager;
        this.applicationContext = applicationContext;
    }

    @Around("@within(org.springframework.stereotype.Component) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.web.bind.annotation.RestController)")
    public Object aroundComponentOrRestController(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        Class<?> targetClass = target.getClass();

        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(CookieSession.class)) {
                injectProxiedCookieData(target, field);
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void injectProxiedCookieData(Object target, Field field) {
        field.setAccessible(true);
        ReactiveRequestContextHolder.getExchange()
                .flatMap(exchange -> exchange.getSession())
                .map(session -> {
                    CookieData cookieData = cookieDataManager.getCookieData(session.getId());
                    if (cookieData == null) {
                        cookieData = createDefaultCookieData(field.getType());
                        cookieDataManager.setCookieData(session.getId(), cookieData);
                    }
                    CookieData proxiedData = CglibProxyFactory.createProxy(cookieData);
                    try {
                        field.set(target, proxiedData);
                    } catch (IllegalAccessException e) {
                        log.error("error={}", e.getMessage(), e);
                    }
                    return proxiedData;
                })
                .subscribe();
    }

    private CookieData createDefaultCookieData(Class<?> type) {
        return (CookieData) applicationContext.getBean(type);
    }
}
