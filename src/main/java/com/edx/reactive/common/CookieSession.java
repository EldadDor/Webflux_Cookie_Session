package com.edx.reactive.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired
@Lazy
public @interface CookieSession {
    boolean required() default true;

    String value() default "";
}
