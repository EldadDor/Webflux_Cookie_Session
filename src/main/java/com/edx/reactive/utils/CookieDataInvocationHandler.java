package com.edx.reactive.utils;

import com.edx.reactive.common.CookieData;
import com.edx.reactive.common.CookieDataWrapper;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;


//@Service
public class CookieDataInvocationHandler implements InvocationHandler {
    private final CookieData target;
    private boolean modified = false;

    public CookieDataInvocationHandler(CookieData target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith("set")) {
            modified = true;
        }
        return method.invoke(target, args);
    }

    public boolean isModified() {
        return modified;
    }

}
