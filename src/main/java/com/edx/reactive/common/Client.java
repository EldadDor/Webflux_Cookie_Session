package com.edx.reactive.common;

import java.util.concurrent.CompletableFuture;

public class Client extends CompletableFuture implements CookieData {
    private String id;
    private String firstName;
    private String lastName;
    private Portfolio portfolio;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

// Getters and setters
}
