package com.edx.reactive.utils;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieDataWrapper;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;


public class CookieDataProxyCreator {
    public <T extends CookieData> T createProxy(CookieDataWrapper<T> wrapper, Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] { type },
                new CookieDataInvocationHandler<>(wrapper)
        );
    }

    private static class CookieDataInvocationHandler<T extends CookieData> implements InvocationHandler {
        private final CookieDataWrapper<T> wrapper;

        public CookieDataInvocationHandler(CookieDataWrapper<T> wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().startsWith("set")) {
                wrapper.setChanged(true);
            }
            return method.invoke(wrapper.getData(), args);
        }
    }
}
