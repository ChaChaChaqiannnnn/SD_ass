package com.shopease.strategy;

/**
 * GoF Strategy — Context role (payment).
 * Holds a {@link ShopEasePaymentStrategy} and delegates via {@link #contextInterface(double)}.
 */
public class ShopEasePaymentContext {
    private ShopEasePaymentStrategy strategy;

    public ShopEasePaymentContext(ShopEasePaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ShopEasePaymentStrategy strategy) {
        this.strategy = strategy;
    }

    /** GoF — delegates to strategy.algorithmInterface(). */
    public void contextInterface(double amount) {
        strategy.algorithmInterface(amount);
    }
}
