package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/** Observer: admin login summary of low / out-of-stock products. */
public class ShopEaseAdminLoginStockObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseAdminLoginStockObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!"ADMIN_LOGIN_STOCK".equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                "<html><body style='width:320px'>" + productName.replace("\n", "<br>") + "</body></html>",
                "Inventory needs attention",
                JOptionPane.WARNING_MESSAGE));
    }
}
