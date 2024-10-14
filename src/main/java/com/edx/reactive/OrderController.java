package com.edx.reactive;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class OrderController {
    @CookieSession
    private Order order;

    @GetMapping("/order")
    public Mono<String> getOrder() {
        return Mono.just(String.format("Order: %s, Items: %d, Total: %.1f",
                order.getId(), order.getItems().size(), order.getTotal()));
    }

    @PostMapping("/order/add")
    public Mono<String> addItem(@RequestBody OrderItem item) {
        order.getItems().add(item);
        order.setTotal(order.getTotal() + (item.getPrice() * item.getQuantity()));
        return Mono.just(String.format("Item added: %s, Quantity: %d, Price: %.1f",
                item.getName(), item.getQuantity(), item.getPrice()));
    }
}
