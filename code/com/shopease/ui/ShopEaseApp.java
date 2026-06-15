package com.shopease.ui;

import com.shopease.model.Admin;
import com.shopease.observer.ShopEaseAdminLoginStockAlertObserver;
import com.shopease.observer.ShopEaseCustomerCartReminderObserver;
import com.shopease.observer.Observer;
import com.shopease.observer.ShopEaseCustomerWishlistRestockObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import java.awt.*;

/**
 * Main window — starts at login/sign-up, then opens Admin or Customer dashboard.
 * <p>
 * Flow to explain: Login → attach the right observers → publish login events → show dashboard.
 */
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
        setMinimumSize(new Dimension(420, 500));
        setLocationRelativeTo(null);
        setTitle("ShopEase — Sign in");
        authLayout.show(authContainer, CARD_LOGIN);
        loginPanel.clearFields();
        revalidate();
        repaint();
    }

    private void showSignUpScreen() {
        setContentPane(authContainer);
        setSize(480, 620);
        setMinimumSize(new Dimension(420, 550));
        setLocationRelativeTo(null);
        setTitle("ShopEase — Create account");
        authLayout.show(authContainer, CARD_SIGNUP);
        signUpPanel.clearFields();
        revalidate();
        repaint();
    }

    /** Called after a successful login — wires up observers and swaps to the correct dashboard. */
    private void onLoginSuccess() {
        setSize(960, 640);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);
        if (service.getCurrentUser() instanceof Admin) {
            setTitle("ShopEase — Admin");
            // Observer — popup summarising all low/out-of-stock products when admin signs in
            Observer adminLoginStockObserver = new ShopEaseAdminLoginStockAlertObserver(this);
            service.attachObserver(adminLoginStockObserver);
            service.publishAdminLowStockOnLogin();
            setContentPane(new AdminDashboard(service, () -> {
                service.detachObserver(adminLoginStockObserver); // clean up on logout
                showLoginScreen();
            }));
        } else {
            setTitle("ShopEase — Shop");
            // Observers — cart reminder + wishlist restock popups for customers on login
            Observer cartReminderObserver = new ShopEaseCustomerCartReminderObserver(this);
            Observer wishlistRestockObserver = new ShopEaseCustomerWishlistRestockObserver(this);
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
        // Fix for High DPI (scaling) issues on Windows
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.setProperty("sun.java2d.dpiaware", "true");
            System.setProperty("sun.java2d.uiScale", "1.5"); // Adjust 1.25, 1.5, or 2.0 based on screen size
        }

        SwingUtilities.invokeLater(() -> {
            // Use Nimbus — cross-platform, ships with every JDK, respects setBackground()
            // on custom-painted buttons (unlike the Windows native L&F which ignores it).
            boolean nimbusDone = false;
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        nimbusDone = true;
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Nimbus unavailable — fall through to cross-platform default
            }
            if (!nimbusDone) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {
                    // use whatever JVM default remains
                }
            }
            new ShopEaseApp().setVisible(true);
        });
    }
}
