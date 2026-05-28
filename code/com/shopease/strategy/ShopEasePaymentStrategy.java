package com.shopease.strategy;

/**
 * Strategy Pattern — common interface for all payment methods.
 * Each method (Credit Card, DuitNow, MAE, TNG) implements execute() differently.
 */
public interface ShopEasePaymentStrategy {
    void execute(double amount);
}
