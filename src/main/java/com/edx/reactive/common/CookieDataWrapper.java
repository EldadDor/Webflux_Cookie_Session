package com.edx.reactive.common;

import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

public class CookieDataWrapper<T extends CookieData> {
    private T data;
    private boolean changed = false;

    public CookieDataWrapper(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
        this.changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}