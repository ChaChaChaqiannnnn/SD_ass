package com.shopease.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern — the "subject" (the thing being watched).
 * When stock changes or the app fires an event, every attached observer gets notified.
 */
public class ShopEaseInventorySubject implements ShopEaseSubject {
    // List of observers waiting to hear about stock or app events
    private List<ShopEaseInventoryObserver> observers = new ArrayList<>();
    private int stock = 5;

    @Override
    public void attach(ShopEaseInventoryObserver o) {
        observers.add(o); // e.g. popup observer, UI refresh observer, cart sync observer
    }

    @Override
    public void detach(ShopEaseInventoryObserver o) {
        observers.remove(o); // called on logout so observers don't leak
    }

    @Override
    public void notifyObservers(String event, String productName) {
        // Loop through every observer and let it decide what to do with this event
        for (ShopEaseInventoryObserver o : observers) {
            o.update(event, productName);
        }
    }

    /** Generic event bus — used for login reminders, UI refresh, not just stock. */
    public void publishEvent(String event, String detail) {
        notifyObservers(event, detail);
    }

    /**
     * Called after admin restock/reduce or checkout.
     * Fires LOW_STOCK (below 5) or OUT_OF_STOCK (zero) so alert observers can react.
     */
    public void setStock(int qty, String productName) {
        this.stock = qty;
        if (qty == 0) {
            System.out.println("\n[Inventory] " + productName + " is now OUT OF STOCK.");
            notifyObservers(ShopEaseAppEvents.OUT_OF_STOCK, productName);
        } else if (qty < com.shopease.model.InventoryStockStatus.LOW_STOCK_THRESHOLD) {
            System.out.println("\n[Inventory] " + productName + " stock is LOW (" + qty + ").");
            notifyObservers(ShopEaseAppEvents.LOW_STOCK, productName);
        }
    }
}
