package com.shopease.observer;

//this is the rule for any object that wants to get inventory alerts
public interface ShopEaseInventoryObserver {
    //this is what the object does when it gets an update
    void update(String event, String productName);
}
