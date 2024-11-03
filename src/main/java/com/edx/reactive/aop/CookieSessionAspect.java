package com.edx.reactive.aop;
import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.spring.BeanTypeResolver;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.lang.reflect.Field;


@Aspect
@Component
public class CookieSessionAspect {

    private static final Logger log = LogManager.getLogger(CookieSessionAspect.class);

    private final CookieDataManager cookieDataManager;
    private final BeanTypeResolver beanTypeResolver;


    public CookieSessionAspect(CookieDataManager cookieDataManager, BeanTypeResolver beanTypeResolver) {
        this.cookieDataManager = cookieDataManager;
        this.beanTypeResolver = beanTypeResolver;
    }


    @Around("@within(org.springframework.stereotype.Component) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.web.bind.annotation.RestController)")
    public Object aroundComponentOrRestController(ProceedingJoinPoint joinPoint) throws Throwable {

//        log.info("aroundComponentOrRestController Before");
        Object target = joinPoint.getTarget();
        Class<?> targetClass = target.getClass();


//        ServerWebExchange exchange = ReactiveRequestContextHolder.getExchange();

        return ReactiveRequestContextHolder.getExchange()
                .flatMap(exchange -> {
                    // Do something with exchange
                    return Mono.defer(() -> {
                        try {
                            for (Field field : targetClass.getDeclaredFields()) {
                                if (field.isAnnotationPresent(CookieSession.class)) {
                                    injectProxiedCookieData(target, field, exchange);
                                }
                            }
                            Object result = joinPoint.proceed();
                            if (result instanceof Mono) {
                                return (Mono<?>) result;
                            }
                            return Mono.just(result);
                        } catch (Throwable t) {
                            return Mono.error(t);
                        }
                    });
                });



     /*   if (exchange == null) {
            return joinPoint.proceed();
        }
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(CookieSession.class)) {
                injectProxiedCookieData(target, field, exchange);
            }
        }
        try {
            Object proceed = joinPoint.proceed();
//            log.info("aroundComponentOrRestController After");
            return proceed;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }*/
    }

    private void injectProxiedCookieData(Object target, Field field, Mono<ServerWebExchange> exchange) {
        exchange.flatMap(ServerWebExchange::getSession)
                .flatMap(session -> {
                    String sessionId = session.getId();
                    CookieData cookieData = cookieDataManager.getCookieData(sessionId);
                    if (cookieData == null) {
                        cookieData = (CookieData) beanTypeResolver.createInstance(field.getType(), field);
                    }
                    CookieData proxiedData = CglibProxyFactory.createProxy(cookieData);
                    try {
                        field.setAccessible(true);
                        field.set(target, proxiedData);
                        cookieDataManager.setCookieData(sessionId, proxiedData);
                        return Mono.empty();
                    } catch (IllegalAccessException e) {
                        log.error("error={}", e.getMessage(), e);
                        return Mono.error(e);
                    }
                })
                .subscribe();
    }


    private void injectProxiedCookieData(Object target, Field field, ServerWebExchange exchange) {
        exchange.getSession().subscribe(session -> {
            String sessionId = session.getId();
            CookieData cookieData = cookieDataManager.getCookieData(sessionId);
            if (cookieData == null) {
                cookieData = (CookieData) beanTypeResolver.createInstance(field.getType(), field);
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


    private boolean isSwaggerComponent(Object target) {
        String className = target.getClass().getName();
        return className.contains("springfox") ||
                className.contains("swagger") ||
                className.contains("springdoc");
    }
}

