package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * GoF Observer — ConcreteObserver (admin login low-stock summary popup).
 */
public class ShopEaseAdminLoginStockAlertObserver extends ShopEaseAbstractObserver {
    private final Component parent;

    public ShopEaseAdminLoginStockAlertObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    protected void onStateChanged(SubjectState state) {
        if (!ShopEaseAppEvents.ADMIN_LOGIN_STOCK.equals(state.getEvent())) {
            return;
        }
        String detail = state.getProductName();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                "<html><body style='width:320px'>" + detail.replace("\n", "<br>") + "</body></html>",
                "Inventory needs attention",
                JOptionPane.WARNING_MESSAGE));
    }
}
