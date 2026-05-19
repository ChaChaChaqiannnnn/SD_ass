package com.shopease.strategy;

//this is the plan for how all payments should work
public interface ShopEasePaymentStrategy {
    //this is the action that finishes the payment
    void execute(double amount);
}
