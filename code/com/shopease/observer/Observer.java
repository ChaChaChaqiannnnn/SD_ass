package com.shopease.observer;

//this is the rule for any object that wants to watch the inventory
public interface Observer {
    //this is what happens when something changes
    void update(String event, String productName);
}
