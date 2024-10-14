package com.edx.reactive;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CookieDataProxyCreator {

	@SuppressWarnings("unchecked")
	public <T> T createProxy(CookieDataWrapper<?> wrapper, Class<T> type) {
		if (type.isInterface()) {
			// Use JDK dynamic proxy for interfaces
			return (T) Proxy.newProxyInstance(
					type.getClassLoader(),
					new Class<?>[]{type},
					(proxy, method, args) -> {
						if (method.getName().startsWith("set")) {
							wrapper.setChanged(true);
						}
						return method.invoke(wrapper.getData(), args);
					}
			);
		} else {
			// Use CGLIB proxy for concrete classes
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(type);
			enhancer.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					if (method.getName().startsWith("set")) {
						wrapper.setChanged(true);
					}
					if (wrapper.getData() == null) {
						return proxy.invoke(wrapper, args);
					}
					return proxy.invoke(wrapper.getData(), args);
				}
			});
			return (T) enhancer.create();
		}
	}
}