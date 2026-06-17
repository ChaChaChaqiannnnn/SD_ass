package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * GoF Observer — ConcreteObserver (admin GUI stock alert popup).
 */
public class ShopEaseInventoryAdminAlertObserver extends ShopEaseAbstractObserver {
    private final Component parent;

    public ShopEaseInventoryAdminAlertObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    protected void onStateChanged(SubjectState state) {
        String event = state.getEvent();
        String productName = state.getProductName();
        if (!ShopEaseAppEvents.LOW_STOCK.equals(event) && !ShopEaseAppEvents.OUT_OF_STOCK.equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            String title = "Stock alert";
            String message;
            int type = JOptionPane.WARNING_MESSAGE;
            if (ShopEaseAppEvents.OUT_OF_STOCK.equals(event)) {
                message = productName + " is now OUT OF STOCK. Restock immediately.";
                type = JOptionPane.ERROR_MESSAGE;
            } else {
                message = productName + " is LOW on stock. Consider restocking soon.";
            }
            JOptionPane.showMessageDialog(parent, message, title, type);
        });
    }
}
