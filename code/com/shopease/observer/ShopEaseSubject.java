package com.shopease.observer;

//this is the set of rules for any object that wants to be watched
public interface ShopEaseSubject {
    //this is how we add someone to the watch list
    void attach(ShopEaseInventoryObserver o);

    //this is how we remove someone from the watch list
    void detach(ShopEaseInventoryObserver o);

    //this is how we tell everyone on the list that something happened
    void notifyObservers(String event, String productName);
}
