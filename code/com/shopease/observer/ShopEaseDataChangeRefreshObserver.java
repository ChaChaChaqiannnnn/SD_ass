package com.shopease.observer;

import javax.swing.SwingUtilities;

/**
 * Observer — DataChangeRefreshObserver
 * Keeps screens in sync: when data changes (cart, stock, wishlist), re-runs a refresh method.
 * Used by CustomerDashboard, AdminDashboard, and CartDialog.
 */
public class ShopEaseDataChangeRefreshObserver implements ShopEaseInventoryObserver {
    private final Runnable onRefresh;

    public ShopEaseDataChangeRefreshObserver(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    @Override
    public void update(String event, String productName) {
        if (ShopEaseAppEvents.DATA_CHANGED.equals(event)) {
            SwingUtilities.invokeLater(onRefresh);
        }
    }
}
