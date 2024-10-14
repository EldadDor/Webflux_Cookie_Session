package com.edx.reactive;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;


public class ExtendedCookieDataBeanPostProcessor extends CommonAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {
	private final ApplicationContext applicationContext;
	private final CookieDataProxyCreator proxyCreator;


	public ExtendedCookieDataBeanPostProcessor(ApplicationContext applicationContext, CookieDataProxyCreator proxyCreator) {
		this.applicationContext = applicationContext;
		this.proxyCreator = proxyCreator;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Object processedBean = super.postProcessBeforeInitialization(bean, beanName);
		return injectCookieData(processedBean, beanName);
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) {
		return super.postProcessAfterInstantiation(bean, beanName);
	}

	private Object injectCookieData(Object bean, String beanName) {
		Class<?> beanClass = bean.getClass();
		for (Field field : beanClass.getDeclaredFields()) {
			boolean cookieDataField = isCookieDataField(field);
			if (cookieDataField) {
				injectCookieDataProxy(bean, field);
			}
		}
		return bean;
	}

	private boolean isCookieDataField(Field field) {
		return field.isAnnotationPresent(CookieSession.class);
	}

	private boolean isCookieDataType(Class<?> type) {
		return CookieData.class.isAssignableFrom(type);
	}

	private void injectCookieDataProxy(Object bean, Field field) {
		field.setAccessible(true);
		try {
			Class<?> type = field.getType();
			if (isCookieDataType(type)) {
				CookieDataWrapper<?> wrapper = applicationContext.getBean(CookieDataWrapper.class, type);
				Object proxy = proxyCreator.createProxy(wrapper, type);
				field.set(bean, proxy);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error injecting cookie data proxy", e);
		}
	}

	@Override
	public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return SmartInstantiationAwareBeanPostProcessor.super.predictBeanType(beanClass, beanName);
	}

	@Override
	public Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return SmartInstantiationAwareBeanPostProcessor.super.determineBeanType(beanClass, beanName);
	}

	@Override
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
		return SmartInstantiationAwareBeanPostProcessor.super.determineCandidateConstructors(beanClass, beanName);
	}

	@Override
	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return SmartInstantiationAwareBeanPostProcessor.super.getEarlyBeanReference(bean, beanName);
	}
}
