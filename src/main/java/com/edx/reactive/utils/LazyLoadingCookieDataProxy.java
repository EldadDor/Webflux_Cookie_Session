package com.edx.reactive.utils;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieDataWrapper;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.context.ApplicationContext;
import java.lang.reflect.Method;


public class LazyLoadingCookieDataProxy<T extends CookieData> implements InvocationHandler {
    private final ApplicationContext applicationContext;
    private final CookieDataProxyCreator proxyCreator;
    private final Class<T> type;
    private T target;

    public LazyLoadingCookieDataProxy(ApplicationContext applicationContext, CookieDataProxyCreator proxyCreator, Class<T> type) {
        this.applicationContext = applicationContext;
        this.proxyCreator = proxyCreator;
        this.type = type;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (target == null) {
            CookieDataWrapper<T> wrapper = applicationContext.getBean(CookieDataWrapper.class);
            target = proxyCreator.createProxy(wrapper, type);
        }
        return method.invoke(target, args);
    }
}
