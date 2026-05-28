package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Observer — AdminLoginStockAlertObserver
 * One-time popup when admin logs in listing every product that needs restocking.
 * Triggered by publishAdminLowStockOnLogin() in ShopEaseService.
 */
public class ShopEaseAdminLoginStockAlertObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseAdminLoginStockAlertObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!ShopEaseAppEvents.ADMIN_LOGIN_STOCK.equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                "<html><body style='width:320px'>" + productName.replace("\n", "<br>") + "</body></html>",
                "Inventory needs attention",
                JOptionPane.WARNING_MESSAGE));
    }
}
