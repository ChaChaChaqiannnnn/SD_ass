package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Observer — CustomerWishlistRestockObserver
 * Tells the customer when something on their wishlist was out of stock but is available again.
 */
public class ShopEaseCustomerWishlistRestockObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseCustomerWishlistRestockObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!ShopEaseAppEvents.WISHLIST_RESTOCK.equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                "<html><body style='width:300px'>Good news — these wishlist items are back in stock:<br><br>"
                        + productName.replace("\n", "<br>") + "</body></html>",
                "Wishlist restocked",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
