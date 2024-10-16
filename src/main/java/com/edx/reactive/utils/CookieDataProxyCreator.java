package com.edx.reactive.utils;

import com.edx.reactive.common.CookieDataWrapper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Component;

@Component
public class CookieDataProxyCreator {

	@SuppressWarnings("unchecked")
	public <T> T createProxy(CookieDataWrapper<T> wrapper, Class<T> type) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
			if (method.getName().startsWith("set")) {
				wrapper.setChanged(true);
			}
			T data = wrapper.getData();
			if (data == null) {
				data = type.getDeclaredConstructor().newInstance();
				wrapper.setData(data);
			}
			return method.invoke(data, args);
		});
		return (T) enhancer.create();
	}
}
