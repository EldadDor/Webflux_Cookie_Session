package com.edx.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.edx.reactive")
public class CookieInjectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookieInjectionApplication.class, args);
    }

}
