package com.shopease.observer;

// Observer Pattern — any class that wants to "listen" must implement this one method
public interface Observer {
    /** event = what happened (e.g. LOW_STOCK), productName = product name or extra detail */
    void update(String event, String productName);
}
