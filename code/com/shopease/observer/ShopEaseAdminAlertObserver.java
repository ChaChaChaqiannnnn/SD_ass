package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/** Observer: admin popup alerts for low / out of stock (inventory). */
public class ShopEaseAdminAlertObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseAdminAlertObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!"LOW_STOCK".equals(event) && !"OUT_OF_STOCK".equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            String title = "Stock alert";
            String message;
            int type = JOptionPane.WARNING_MESSAGE;
            if ("OUT_OF_STOCK".equals(event)) {
                message = productName + " is now OUT OF STOCK. Restock immediately.";
                type = JOptionPane.ERROR_MESSAGE;
            } else {
                message = productName + " is LOW on stock. Consider restocking soon.";
            }
            JOptionPane.showMessageDialog(parent, message, title, type);
        });
    }
}
