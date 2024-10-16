package com.edx.reactive.http;

import com.edx.reactive.common.CookieSession;
import com.edx.reactive.common.Order;
import com.edx.reactive.common.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @CookieSession
    private Order order;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping
    public Mono<String> getOrder() {
        ensureOrderInitialized();
        return Mono.just(String.format("Order: %s, Items: %d, Total: %.2f",
                order.getId(), order.getItems().size(), order.getTotal()));
    }

    @PostMapping("/add")
    public Mono<String> addItem(@RequestBody OrderItem item) {
        ensureOrderInitialized();
        order.getItems().add(item);
        order.setTotal(order.getTotal() + (item.getPrice() * item.getQuantity()));
        return Mono.just(String.format("Item added: %s, Quantity: %d, Price: %.2f",
                item.getName(), item.getQuantity(), item.getPrice()));
    }

    private void ensureOrderInitialized() {
        if (order == null) {
            order = applicationContext.getBean(Order.class);
        }
    }
}
