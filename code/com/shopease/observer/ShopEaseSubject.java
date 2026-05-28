package com.shopease.observer;

/**
 * Observer Pattern — the "subject" side of the contract.
 * Think of attach/detach as subscribe/unsubscribe to notifications.
 */
public interface ShopEaseSubject {
    void attach(ShopEaseInventoryObserver o);
    void detach(ShopEaseInventoryObserver o);
    void notifyObservers(String event, String productName);
}
