package com.shopease.strategy;

//this is the way to pay using duitnow qr
public class ShopEaseDuitNowStrategy implements ShopEasePaymentStrategy {
    @Override
    public void execute(double amount) {
        //this shows that the duitnow payment was successful
        System.out.println("Processing Payment: RM" + String.format("%.2f", amount) + " via DuitNow QR.");
    }
}
