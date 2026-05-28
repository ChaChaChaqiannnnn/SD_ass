package com.shopease.observer;

/**
 * Observer Pattern — ShopEase-specific observer interface.
 * Every *FunctionObserver class in this package implements this.
 */
public interface ShopEaseInventoryObserver extends Observer {
    @Override
    void update(String event, String productName);
}
