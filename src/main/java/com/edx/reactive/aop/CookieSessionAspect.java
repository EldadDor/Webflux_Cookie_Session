package com.edx.reactive.aop;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.common.DefaultCookieData;
import com.edx.reactive.spring.CookieSessionBeanPostProcessor;
import com.edx.reactive.utils.CglibProxyFactory;
import com.edx.reactive.utils.CookieDataManager;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        log.info("aroundComponentOrRestController Before");
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
//            throw new ServerErrorException(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void injectProxiedCookieData(Object target, Field field) {
        log.info("injectProxiedCookieData target={}", target);
        ServerWebExchange exchange = ReactiveRequestContextHolder.getExchange();
        if (exchange != null) {
            exchange.getSession().subscribe(session -> {
                String sessionId = session.getId();
                CookieData cookieData = cookieDataManager.getCookieData(sessionId);
                if (cookieData == null) {
                    log.info("CookieData is NULL");
                    cookieData = createDefaultCookieData(field.getType());
                }
                CookieData proxiedData = CglibProxyFactory.createProxy(cookieData);
                try {
                    field.setAccessible(true);
                    field.set(target, proxiedData);
                    cookieDataManager.setCookieData(sessionId, proxiedData);
                } catch (IllegalAccessException e) {
                    log.error("error={}", e.getMessage(), e);
                }
            });
        }
    }


    private CookieData createDefaultCookieData(Class<?> type) {
        if (applicationContext.getBeansOfType(type).size() > 1) {
            return (CookieData) applicationContext.getBeansOfType(type).entrySet().stream().findFirst().get().getValue();
        }
        return (CookieData) applicationContext.getBean(type);
    }
}
