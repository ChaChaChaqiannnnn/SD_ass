package com.shopease.strategy;

// Strategy Pattern — DuitNow QR payment method
public class ShopEaseDuitNowStrategy implements ShopEasePaymentStrategy {
    @Override
    public void execute(double amount) {
        //this shows that the duitnow payment was successful
        System.out.println("Processing Payment: RM" + String.format("%.2f", amount) + " via DuitNow QR.");
    }
}
