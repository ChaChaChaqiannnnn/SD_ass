package com.shopease.ui;

import com.shopease.model.Admin;
import com.shopease.observer.ShopEaseAdminLoginStockObserver;
import com.shopease.observer.ShopEaseCartReminderObserver;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseWishlistRestockObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import java.awt.*;

public class ShopEaseApp extends JFrame {
    private static final String CARD_LOGIN = "login";
    private static final String CARD_SIGNUP = "signup";

    private final ShopEaseService service;
    private final CardLayout authLayout;
    private final JPanel authContainer;
    private LoginPanel loginPanel;
    private SignUpPanel signUpPanel;

    public ShopEaseApp() {
        this.service = new ShopEaseService();
        setTitle("ShopEase Malaysia");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        authLayout = new CardLayout();
        authContainer = new JPanel(authLayout);

        loginPanel = new LoginPanel(service, this::onLoginSuccess, this::showSignUpScreen);
        signUpPanel = new SignUpPanel(service,
                () -> {
                    showLoginScreen();
                    loginPanel.showStatus("Account created! Sign in with your email and password.", false);
                },
                this::showLoginScreen);

        authContainer.add(loginPanel, CARD_LOGIN);
        authContainer.add(signUpPanel, CARD_SIGNUP);

        showLoginScreen();
    }

    private void showLoginScreen() {
        setContentPane(authContainer);
        setSize(480, 560);
        setTitle("ShopEase — Sign in");
        authLayout.show(authContainer, CARD_LOGIN);
        loginPanel.clearFields();
        revalidate();
        repaint();
    }

    private void showSignUpScreen() {
        setContentPane(authContainer);
        setSize(480, 620);
        setTitle("ShopEase — Create account");
        authLayout.show(authContainer, CARD_SIGNUP);
        signUpPanel.clearFields();
        revalidate();
        repaint();
    }

    private void onLoginSuccess() {
        setSize(900, 600);
        if (service.getCurrentUser() instanceof Admin) {
            setTitle("ShopEase — Admin");
            ShopEaseInventoryObserver adminLoginStockObserver = new ShopEaseAdminLoginStockObserver(this);
            service.attachObserver(adminLoginStockObserver);
            service.publishAdminLowStockOnLogin();
            setContentPane(new AdminDashboard(service, () -> {
                service.detachObserver(adminLoginStockObserver);
                showLoginScreen();
            }));
        } else {
            setTitle("ShopEase — Shop");
            ShopEaseInventoryObserver cartReminderObserver = new ShopEaseCartReminderObserver(this);
            ShopEaseInventoryObserver wishlistRestockObserver = new ShopEaseWishlistRestockObserver(this);
            service.attachObserver(cartReminderObserver);
            service.attachObserver(wishlistRestockObserver);
            service.publishWishlistRestockOnLogin();
            service.publishCartReminderIfNeeded();
            setContentPane(new CustomerDashboard(service, () -> {
                service.detachObserver(cartReminderObserver);
                service.detachObserver(wishlistRestockObserver);
                showLoginScreen();
            }));
            revalidate();
            repaint();
            return;
        }
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // use default L&F
            }
            new ShopEaseApp().setVisible(true);
        });
    }
}
