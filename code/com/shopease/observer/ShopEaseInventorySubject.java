package com.shopease.observer;

/**
 * GoF Observer — ConcreteSubject.
 * Maintains {@code subjectState} and notifies observers when inventory or app events change.
 */
public class ShopEaseInventorySubject extends Subject {
    private SubjectState subjectState = SubjectState.empty();
    private int stock = 0;

    /** GoF — {@code return subjectState}. */
    @Override
    public SubjectState getState() {
        return subjectState;
    }

    private void setSubjectState(String event, String productName) {
        subjectState = new SubjectState(event, productName, stock);
    }

    /** Generic event bus — login reminders, UI refresh, not just stock. */
    public void publishEvent(String event, String detail) {
        setSubjectState(event, detail);
        notifyObservers();
    }

    /**
     * Called after admin restock/reduce or checkout.
     * Fires LOW_STOCK or OUT_OF_STOCK so alert observers can react.
     */
    public void setStock(int qty, String productName) {
        this.stock = qty;
        if (qty == 0) {
            System.out.println("\n[Inventory] " + productName + " is now OUT OF STOCK.");
            setSubjectState(ShopEaseAppEvents.OUT_OF_STOCK, productName);
            notifyObservers();
        } else if (qty < com.shopease.model.InventoryStockStatus.LOW_STOCK_THRESHOLD) {
            System.out.println("\n[Inventory] " + productName + " stock is LOW (" + qty + ").");
            setSubjectState(ShopEaseAppEvents.LOW_STOCK, productName);
            notifyObservers();
        }
    }
}
