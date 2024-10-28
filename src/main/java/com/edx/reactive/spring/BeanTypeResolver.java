package com.edx.reactive.spring;

import com.edx.reactive.common.CookieSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

@Component
@Slf4j
public class BeanTypeResolver {
    private final ApplicationContext applicationContext;

    public BeanTypeResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T> T createInstance(Class<T> type, Field field) {
        CookieSession annotation = field.getAnnotation(CookieSession.class);
        String beanName = annotation != null ? annotation.beanName() : "";

        if (!beanName.isEmpty()) {
            return (T) applicationContext.getBean(beanName);
        }

        if (type.isInterface()) {
            Map<String, T> implementations = applicationContext.getBeansOfType(type);
            if (implementations.isEmpty()) {
                throw new IllegalStateException("No implementation found for interface: " + type.getName());
            }
            return implementations.values().iterator().next();
        }

        return applicationContext.getBean(type);
    }
}
