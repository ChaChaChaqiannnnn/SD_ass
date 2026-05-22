package com.shopease.observer;

/** Observer interface for inventory, UI sync, cart reminders, and admin alerts. */
public interface ShopEaseInventoryObserver extends Observer {
    @Override
    void update(String event, String productName);
}
