package com.edx.reactive.http;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibProxyFactory {
    public static <T> T createProxy(Class<T> targetClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        ModifyingMethodInterceptor interceptor = new ModifyingMethodInterceptor();
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }

    static class ModifyingMethodInterceptor implements MethodInterceptor {
        private boolean modified = false;

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (method.getName().startsWith("set")) {
                modified = true;
            }
            return proxy.invokeSuper(obj, args);
        }

        public boolean isModified() {
            return modified;
        }
    }
}
