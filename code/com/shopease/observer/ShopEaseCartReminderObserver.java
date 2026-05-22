package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/** Observer: notifies customer when items remain in cart (Singleton cart). */
public class ShopEaseCartReminderObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseCartReminderObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!"CART_REMINDER".equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                productName,
                "Cart reminder",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
