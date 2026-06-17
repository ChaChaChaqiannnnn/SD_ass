package com.shopease.observer;

import javax.swing.SwingUtilities;

/**
 * GoF Observer — ConcreteObserver (UI refresh on DATA_CHANGED).
 */
public class ShopEaseDataChangeRefreshObserver extends ShopEaseAbstractObserver {
    private final Runnable onRefresh;

    public ShopEaseDataChangeRefreshObserver(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    @Override
    protected void onStateChanged(SubjectState state) {
        if (ShopEaseAppEvents.DATA_CHANGED.equals(state.getEvent())) {
            SwingUtilities.invokeLater(onRefresh);
        }
    }
}
