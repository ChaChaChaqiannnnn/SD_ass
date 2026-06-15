package com.shopease.observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * GoF Observer — ConcreteObserver (customer cart reminder on login).
 */
public class ShopEaseCustomerCartReminderObserver extends ShopEaseAbstractObserver {
    private final Component parent;

    public ShopEaseCustomerCartReminderObserver(Component parent) {
        this.parent = parent;
    }

    @Override
    protected void onStateChanged(SubjectState state) {
        if (!ShopEaseAppEvents.CART_REMINDER.equals(state.getEvent())) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                state.getProductName(),
                "Cart reminder",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
