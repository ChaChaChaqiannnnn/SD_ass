package com.shopease.ui;

import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Dedicated sign-in screen — separate from registration. */
public class LoginPanel extends JPanel {
    private final ShopEaseService service;
    private final Runnable onLoginSuccess;
    private final Runnable onGoToSignUp;
    private final JLabel statusLabel;
    private final JTextField emailField;
    private final JPasswordField passwordField;

    public LoginPanel(ShopEaseService service, Runnable onLoginSuccess, Runnable onGoToSignUp) {
        this.service = service;
        this.onLoginSuccess = onLoginSuccess;
        this.onGoToSignUp = onGoToSignUp;

        setLayout(new BorderLayout());
        setBackground(ShopEaseUIUtils.BG_PAGE);
        setBorder(new EmptyBorder(32, 48, 32, 48));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232)),
                new EmptyBorder(32, 36, 32, 36)));
        card.setMaximumSize(new Dimension(400, 500));

        JLabel title = new JLabel("Sign in");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = ShopEaseUIUtils.createMutedLabel("Welcome back to ShopEase Malaysia");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));

        card.add(ShopEaseUIUtils.createFieldLabel("Email"));
        card.add(Box.createVerticalStrut(6));
        emailField = ShopEaseUIUtils.createAuthTextField();
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));

        card.add(ShopEaseUIUtils.createFieldLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        passwordField = ShopEaseUIUtils.createAuthPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));

        statusLabel = ShopEaseUIUtils.createMutedLabel(" ");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(16));

        JButton signInBtn = new JButton("Sign in");
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        ShopEaseUIUtils.stylePrimaryButton(signInBtn);
        signInBtn.addActionListener(e -> attemptLogin());
        card.add(signInBtn);

        passwordField.addActionListener(e -> attemptLogin());

        card.add(Box.createVerticalStrut(20));

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        switchRow.add(ShopEaseUIUtils.createMutedLabel("New here?"));
        JButton signUpLink = ShopEaseUIUtils.createLinkButton("Create an account");
        signUpLink.addActionListener(e -> onGoToSignUp.run());
        switchRow.add(signUpLink);
        card.add(switchRow);

        card.add(Box.createVerticalStrut(16));
        JLabel adminHint = ShopEaseUIUtils.createMutedLabel(
                "<html>Staff admin? Use your admin email (e.g. admin@email.admin.my)</html>");
        adminHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(adminHint);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(card);
        add(centerWrap, BorderLayout.CENTER);
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        showStatus("", false);
    }

    public void showStatus(String message, boolean error) {
        statusLabel.setText(message.isEmpty() ? " " : message);
        statusLabel.setForeground(error ? ShopEaseUIUtils.DANGER : ShopEaseUIUtils.SUCCESS);
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String pass  = new String(passwordField.getPassword());

        // ── Client-side check (no I/O — safe on EDT) ────────────────────────
        if (email.isEmpty() || pass.isEmpty()) {
            showStatus("Please enter your email and password.", true);
            return;
        }

        // ── Database read on background thread (SwingWorker) ────────────────
        // Fixes the Windows "Not Responding" freeze caused by SQLite I/O
        // blocking the Event Dispatch Thread.
        JButton signInBtn = findSignInButton(this);
        if (signInBtn != null) {
            signInBtn.setEnabled(false);
            signInBtn.setText("Signing in…");
        }
        showStatus("Signing in…", false);

        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return service.login(email, pass);
            }

            @Override
            protected void done() {
                // Back on the EDT — safe to touch UI
                if (signInBtn != null) {
                    signInBtn.setEnabled(true);
                    signInBtn.setText("Sign in");
                }
                try {
                    if (get()) {
                        onLoginSuccess.run();
                    } else {
                        String msg = service.getLastMessage().isEmpty()
                                ? "Incorrect email or password. Please try again."
                                : service.getLastMessage();
                        showStatus(msg, true);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    showStatus("An unexpected error occurred. Please try again.", true);
                }
            }
        }.execute();
    }

    /** Recursively searches for a JButton with text "Sign in". */
    private static JButton findSignInButton(java.awt.Container container) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof JButton b && "Sign in".equals(b.getText())) {
                return b;
            }
            if (c instanceof java.awt.Container sub) {
                JButton found = findSignInButton(sub);
                if (found != null) return found;
            }
        }
        return null;
    }
}

