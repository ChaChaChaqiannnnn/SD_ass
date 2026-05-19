package com.shopease.observer;

//this sends alerts to the admin when things go wrong
public class ShopEaseAdminObserver implements ShopEaseInventoryObserver {
    @Override
    public void update(String event, String productName) {
        if (event.equals("OUT_OF_STOCK")) {
            //this tells the admin to buy more stock right now
            System.out.println("[ADMIN ALERT] " + productName + " requires immediate attention!");
        } else if (event.equals("LOW_STOCK")) {
            //this warns the admin that stock is running low
            System.out.println("ADMIN ALERT: " + productName + " requires restock! Event: LOW_STOCK");
        }
    }
}
