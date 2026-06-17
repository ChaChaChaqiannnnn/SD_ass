package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * GoF Observer — ConcreteObserver (customer wishlist restock popup).
 */
public class ShopEaseCustomerWishlistRestockObserver extends ShopEaseAbstractObserver {
    private final Component parent;

    public ShopEaseCustomerWishlistRestockObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    protected void onStateChanged(SubjectState state) {
        if (!ShopEaseAppEvents.WISHLIST_RESTOCK.equals(state.getEvent())) {
            return;
        }
        String detail = state.getProductName();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                "<html><body style='width:300px'>Good news — these wishlist items are back in stock:<br><br>"
                        + detail.replace("\n", "<br>") + "</body></html>",
                "Wishlist restocked",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
