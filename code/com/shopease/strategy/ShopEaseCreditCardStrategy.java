package com.shopease.strategy;

//this is the way to pay using a credit card
public class ShopEaseCreditCardStrategy implements ShopEasePaymentStrategy {
    @Override
    public void execute(double amount) {
        //this shows that the credit card payment was successful
        System.out.println("Processing Payment: RM" + String.format("%.2f", amount) + " via Credit Card.");
    }
}
