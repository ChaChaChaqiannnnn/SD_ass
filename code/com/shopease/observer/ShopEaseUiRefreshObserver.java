package com.shopease.observer;

import javax.swing.SwingUtilities;

/** Observer: refreshes UI when catalog, cart, wishlist, or orders change. */
public class ShopEaseUiRefreshObserver implements ShopEaseInventoryObserver {
    private final Runnable onRefresh;

    public ShopEaseUiRefreshObserver(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    @Override
    public void update(String event, String productName) {
        if ("DATA_CHANGED".equals(event)) {
            SwingUtilities.invokeLater(onRefresh);
        }
    }
}
