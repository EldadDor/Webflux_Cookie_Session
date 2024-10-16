package com.edx.reactive.common;

import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CookieDataWrapper<T> {
	private T data;
	@Setter
	private boolean changed = false;

	public CookieDataWrapper() {
	}

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

	public void resetChanged() {
		changed = false;
	}

}