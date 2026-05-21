package com.shopease.ui;

import com.shopease.model.Customer;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Dedicated registration screen — separate from sign-in. */
public class SignUpPanel extends JPanel {
    private final ShopEaseService service;
    private final Runnable onSignUpSuccess;
    private final Runnable onGoToLogin;
    private final JLabel statusLabel;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;

    public SignUpPanel(ShopEaseService service, Runnable onSignUpSuccess, Runnable onGoToLogin) {
        this.service = service;
        this.onSignUpSuccess = onSignUpSuccess;
        this.onGoToLogin = onGoToLogin;

        setLayout(new BorderLayout());
        setBackground(ShopEaseUIUtils.BG_PAGE);
        setBorder(new EmptyBorder(24, 48, 24, 48));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232)),
                new EmptyBorder(28, 36, 28, 36)));
        card.setMaximumSize(new Dimension(400, 560));

        JLabel title = new JLabel("Create account");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = ShopEaseUIUtils.createMutedLabel("Register as a customer to start shopping");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        card.add(fieldBlock("Full name", nameField = ShopEaseUIUtils.createAuthTextField()));
        card.add(Box.createVerticalStrut(12));
        card.add(fieldBlock("Email", emailField = ShopEaseUIUtils.createAuthTextField()));
        card.add(Box.createVerticalStrut(12));
        card.add(fieldBlock("Password", passwordField = ShopEaseUIUtils.createAuthPasswordField()));
        card.add(Box.createVerticalStrut(12));
        card.add(fieldBlock("Confirm password", confirmPasswordField = ShopEaseUIUtils.createAuthPasswordField()));

        card.add(Box.createVerticalStrut(8));
        statusLabel = ShopEaseUIUtils.createMutedLabel(" ");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(14));

        JButton createBtn = new JButton("Create account");
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        ShopEaseUIUtils.styleSuccessButton(createBtn);
        createBtn.addActionListener(e -> attemptSignUp());
        card.add(createBtn);

        card.add(Box.createVerticalStrut(18));

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        switchRow.add(ShopEaseUIUtils.createMutedLabel("Already have an account?"));
        JButton loginLink = ShopEaseUIUtils.createLinkButton("Sign in");
        loginLink.addActionListener(e -> onGoToLogin.run());
        switchRow.add(loginLink);
        card.add(switchRow);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ShopEaseUIUtils.BG_PAGE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(scroll);
        add(centerWrap, BorderLayout.CENTER);
    }

    private JPanel fieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = ShopEaseUIUtils.createFieldLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    public void clearFields() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        showStatus("", false);
    }

    public void showStatus(String message, boolean error) {
        statusLabel.setText(message.isEmpty() ? " " : message);
        statusLabel.setForeground(error ? ShopEaseUIUtils.DANGER : ShopEaseUIUtils.SUCCESS);
    }

    private void attemptSignUp() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (name.isEmpty()) {
            showStatus("Please enter your name.", true);
            nameField.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            showStatus("Please enter your email.", true);
            emailField.requestFocus();
            return;
        }
        if (!email.contains("@")) {
            showStatus("Please enter a valid email address.", true);
            emailField.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            showStatus("Please enter a password.", true);
            passwordField.requestFocus();
            return;
        }
        if (pass.length() < 4) {
            showStatus("Password must be at least 4 characters.", true);
            passwordField.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            showStatus("Passwords do not match. Please check and try again.", true);
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        Customer customer = new Customer("CUST-" + System.currentTimeMillis(), name, email, pass);
        if (service.registerCustomer(customer)) {
            clearFields();
            onSignUpSuccess.run();
        } else {
            String msg = service.getLastMessage().isEmpty()
                    ? "Could not create account. Please try again." : service.getLastMessage();
            showStatus(msg, true);
        }
    }
}
