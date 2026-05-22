package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/** Observer: customer login alert when wishlist items are back in stock. */
public class ShopEaseWishlistRestockObserver implements ShopEaseInventoryObserver {
    private final Component parent;

    public ShopEaseWishlistRestockObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    public void update(String event, String productName) {
        if (!"WISHLIST_RESTOCK".equals(event)) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                "<html><body style='width:300px'>Good news — these wishlist items are back in stock:<br><br>"
                        + productName.replace("\n", "<br>") + "</body></html>",
                "Wishlist restocked",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
