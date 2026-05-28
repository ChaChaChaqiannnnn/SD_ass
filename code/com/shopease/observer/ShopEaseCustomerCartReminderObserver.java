package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Observer — CustomerCartReminderObserver
 * Friendly popup if the customer still has items in their Singleton cart after login.
 */
public class ShopEaseCustomerCartReminderObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseCustomerCartReminderObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!ShopEaseAppEvents.CART_REMINDER.equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                productName,
                "Cart reminder",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
