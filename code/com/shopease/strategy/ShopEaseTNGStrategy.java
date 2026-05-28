package com.shopease.strategy;

// Strategy Pattern — Touch 'n Go e-wallet payment method
public class ShopEaseTNGStrategy implements ShopEasePaymentStrategy {
    @Override
    public void execute(double amount) {
        //this shows that the tng payment was successful
        System.out.println("Processing Payment: RM" + String.format("%.2f", amount) + " via TNG e-Wallet.");
    }
}
