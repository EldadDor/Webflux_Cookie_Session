package com.edx.reactive.common;

import java.util.List;

public class Portfolio {
    private String portfolioId;
    private double totalValue;
    private List<String> stockSymbols;
    private int numberOfStocks;

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public List<String> getStockSymbols() {
        return stockSymbols;
    }

    public void setStockSymbols(List<String> stockSymbols) {
        this.stockSymbols = stockSymbols;
    }

    public int getNumberOfStocks() {
        return numberOfStocks;
    }

    public void setNumberOfStocks(int numberOfStocks) {
        this.numberOfStocks = numberOfStocks;
    }
}