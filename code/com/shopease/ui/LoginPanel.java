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
        String pass = new String(passwordField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            showStatus("Please enter your email and password.", true);
            return;
        }
        if (service.login(email, pass)) {
            onLoginSuccess.run();
        } else {
            String msg = service.getLastMessage().isEmpty()
                    ? "Incorrect email or password. Please try again." : service.getLastMessage();
            showStatus(msg, true);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
}
