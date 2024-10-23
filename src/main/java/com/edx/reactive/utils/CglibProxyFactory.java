package com.edx.reactive.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cglib.proxy.*;

import java.lang.reflect.Method;

public class CglibProxyFactory {

    private static final Logger log = LogManager.getLogger(CglibProxyFactory.class);


    public static <T> T createProxy(Class<T> targetClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        ModifyingMethodInterceptor interceptor = new ModifyingMethodInterceptor();
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }

    public static boolean isProxy(Object obj) {
        return obj instanceof Factory && ((Factory) obj).getCallbacks().length > 0;
    }

    public static MethodInterceptor getInvocationHandler(Object proxy) {
        if (proxy instanceof Factory) {
            Callback[] callbacks = ((Factory) proxy).getCallbacks();
            if (callbacks.length > 0 && callbacks[0] instanceof MethodInterceptor) {
                return (MethodInterceptor) callbacks[0];
            }
        }
        throw new IllegalArgumentException("The provided object is not a CGLIB proxy");
    }

    public static class ModifyingMethodInterceptor implements MethodInterceptor {
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
