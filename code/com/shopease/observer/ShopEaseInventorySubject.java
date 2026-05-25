package com.shopease.observer;

import java.util.ArrayList;
import java.util.List;

//this class keeps track of how many items are left in the shop
public class ShopEaseInventorySubject implements ShopEaseSubject {
    //this is the list of people or systems waiting for updates
    private List<ShopEaseInventoryObserver> observers = new ArrayList<>();
    private int stock = 5;

    @Override
    public void attach(ShopEaseInventoryObserver o) {
        //this adds a new person to the update list
        observers.add(o);
    }

    @Override
    public void detach(ShopEaseInventoryObserver o) {
        //this removes someone from the update list
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String event, String productName) {
        //this tells everyone on the list that something changed
        for (ShopEaseInventoryObserver o : observers) {
            o.update(event, productName);
        }
    }

    /** Sends out an update to all the listening UI components. */
    public void publishEvent(String event, String detail) {
        notifyObservers(event, detail);
    }

    //this changes the stock amount and sends out alerts
    public void setStock(int qty, String productName) {
        this.stock = qty;
        if (qty == 0) {
            //this shows that we are completely out of items
            System.out.println("\n[Inventory] " + productName + " is now OUT OF STOCK.");
            //this sends a sold out message to everyone
            notifyObservers("OUT_OF_STOCK", productName);
        } else if (qty < com.shopease.model.InventoryStockStatus.LOW_STOCK_THRESHOLD) {
            System.out.println("\n[Inventory] " + productName + " stock is LOW (" + qty + ").");
            //this sends a low stock warning to everyone
            notifyObservers("LOW_STOCK", productName);
        }
    }
}
