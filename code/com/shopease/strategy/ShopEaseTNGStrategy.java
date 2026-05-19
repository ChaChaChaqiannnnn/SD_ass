package com.shopease.strategy;

//this is the way to pay using the tng e-wallet
public class ShopEaseTNGStrategy implements ShopEasePaymentStrategy {
    @Override
    public void execute(double amount) {
        //this shows that the tng payment was successful
        System.out.println("Processing Payment: RM" + String.format("%.2f", amount) + " via TNG e-Wallet.");
    }
}
