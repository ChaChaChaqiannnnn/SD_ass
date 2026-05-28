package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Observer — InventoryAdminAlertObserver
 * Shows a JOptionPane popup on the admin screen whenever stock hits LOW or OUT.
 * Attached in AdminDashboard when admin opens the panel.
 */
public class ShopEaseInventoryAdminAlertObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseInventoryAdminAlertObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
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
