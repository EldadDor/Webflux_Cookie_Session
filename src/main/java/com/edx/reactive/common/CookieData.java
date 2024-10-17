package com.edx.reactive.common;

public interface CookieData {
    default String name() {
        return this.getClass().getSimpleName();
    }
}

