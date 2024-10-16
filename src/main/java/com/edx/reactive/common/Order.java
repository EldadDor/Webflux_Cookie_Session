package com.edx.reactive.common;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Order implements CookieData {
	private String id;
	private List<OrderItem> items;
	private double total;

	public Order() {
	}

	public Order(String id, List<OrderItem> items, double total) {
		this.id = id;
		this.items = items;
		this.total = total;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}
}
