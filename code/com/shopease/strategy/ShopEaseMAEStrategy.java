package com.shopease.strategy;

// Strategy Pattern — MAE app payment method
public class ShopEaseMAEStrategy implements ShopEasePaymentStrategy {
    @Override
    public void execute(double amount) {
        //this shows that the mae payment was successful
        System.out.println("Processing Payment: RM" + String.format("%.2f", amount) + " via MAE App.");
    }
}
