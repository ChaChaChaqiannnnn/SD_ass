package com.shopease.observer;

import com.shopease.singleton.ShopEaseCartSingleton;
import com.shopease.model.CartItem;
import java.util.Iterator;

/**
 * GoF Observer — ConcreteObserver (sync cart when stock hits zero).
 */
public class ShopEaseCartStockSyncObserver extends ShopEaseAbstractObserver {
    private final ShopEaseCartSingleton cart;

    public ShopEaseCartStockSyncObserver(ShopEaseCartSingleton cart) {
        this.cart = cart;
    }

    @Override
    protected void onStateChanged(SubjectState state) {
        if (!ShopEaseAppEvents.OUT_OF_STOCK.equals(state.getEvent())) {
            return;
        }
        String productName = state.getProductName();
        System.out.println("\n[OBSERVER] SYSTEM ALERT: " + productName + " is now out of stock!");

        Iterator<CartItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getName().equalsIgnoreCase(productName)) {
                iterator.remove();
                System.out.println("[OBSERVER] Auto-removed '" + productName
                        + "' from your cart to prevent checkout errors.");
            }
        }
    }
}
