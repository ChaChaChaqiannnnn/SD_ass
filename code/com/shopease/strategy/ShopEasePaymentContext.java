package com.shopease.strategy;

/**
 * Strategy Pattern — the "context" that runs whichever payment strategy was chosen.
 * ShopEaseService.checkout() creates this and calls executeStrategy(total).
 */
public class ShopEasePaymentContext {
    private ShopEasePaymentStrategy strategy;

    //this sets up the chosen payment method
    public ShopEasePaymentContext(ShopEasePaymentStrategy strategy) {
        this.strategy = strategy;
    }

    //this lets the user change their mind on how to pay
    public void setStrategy(ShopEasePaymentStrategy strategy) {
        this.strategy = strategy;
    }

    /** Delegates to Credit Card / DuitNow / MAE / TNG — each has its own execute() logic */
    public void executeStrategy(double amount) {
        strategy.execute(amount);
    }
}
