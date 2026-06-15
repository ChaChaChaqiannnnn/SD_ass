package com.shopease.observer;

/**
 * GoF Observer — ConcreteObserver (inventory admin console log).
 */
public class ShopEaseInventoryAdminLogObserver extends ShopEaseAbstractObserver {

    @Override
    protected void onStateChanged(SubjectState state) {
        String event = state.getEvent();
        String productName = state.getProductName();
        if (ShopEaseAppEvents.OUT_OF_STOCK.equals(event)) {
            System.out.println("[ADMIN ALERT] " + productName + " requires immediate attention!");
        } else if (ShopEaseAppEvents.LOW_STOCK.equals(event)) {
            System.out.println("ADMIN ALERT: " + productName + " requires restock! Event: LOW_STOCK");
        }
    }
}
