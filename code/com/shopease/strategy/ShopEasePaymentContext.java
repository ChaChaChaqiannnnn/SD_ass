package com.shopease.strategy;

//this handles the different ways a user can pay
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

    //this tells the chosen method to start the payment
    public void executeStrategy(double amount) {
        strategy.execute(amount);
    }
}
