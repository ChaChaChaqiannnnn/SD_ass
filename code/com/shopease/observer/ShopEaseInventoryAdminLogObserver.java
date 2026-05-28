package com.shopease.observer;

/**
 * Observer — InventoryAdminLogObserver
 * When stock goes LOW or OUT, prints a message to the console (useful for demo/debug).
 */
public class ShopEaseInventoryAdminLogObserver implements ShopEaseInventoryObserver {
    @Override
    public void update(String event, String productName) {
        if (event.equals(ShopEaseAppEvents.OUT_OF_STOCK)) {
            //this tells the admin to buy more stock right now
            System.out.println("[ADMIN ALERT] " + productName + " requires immediate attention!");
        } else if (event.equals(ShopEaseAppEvents.LOW_STOCK)) {
            //this warns the admin that stock is running low
            System.out.println("ADMIN ALERT: " + productName + " requires restock! Event: LOW_STOCK");
        }
    }
}
