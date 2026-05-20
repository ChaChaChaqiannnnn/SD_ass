package com.shopease.ui;

import com.shopease.model.Admin;
import com.shopease.model.Customer;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ShopEaseApp extends JFrame {
    private ShopEaseService service;

    public ShopEaseApp() {
        this.service = new ShopEaseService();
        setTitle("ShopEase Malaysia");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        showLoginScreen();
    }

    private void showLoginScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("Welcome to ShopEase", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Inter", Font.BOLD, 22));
        headerLabel.setBorder(new EmptyBorder(30, 0, 20, 0));
        headerLabel.setForeground(new Color(44, 62, 80));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(0, 50, 20, 50));

        JTextField emailField = new JTextField();
        emailField.setBorder(BorderFactory.createTitledBorder("Email Address"));
        emailField.setFont(new Font("Inter", Font.PLAIN, 14));
        formPanel.add(emailField);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));
        passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
        formPanel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn, new Color(52, 152, 219));
        
        JButton registerBtn = new JButton("Register New Account");
        styleButton(registerBtn, new Color(149, 165, 166));

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pass = new String(passwordField.getPassword());
            if (service.login(email, pass)) {
                if (service.getCurrentUser() instanceof Admin) {
                    showAdminDashboard();
                } else {
                    showCustomerDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pass = new String(passwordField.getPassword());
            if (!email.isEmpty() && !pass.isEmpty()) {
                Customer c = new Customer("CUST-" + System.currentTimeMillis(), "New User", email, pass);
                service.registerCustomer(c);
                JOptionPane.showMessageDialog(this, "Registration Successful! Please login.");
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in email and password.");
            }
        });

        formPanel.add(loginBtn);
        formPanel.add(registerBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private void showCustomerDashboard() {
        setContentPane(new CustomerDashboard(service, this::showLoginScreen));
        revalidate();
        repaint();
    }

    private void showAdminDashboard() {
        setContentPane(new AdminDashboard(service, this::showLoginScreen));
        revalidate();
        repaint();
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ShopEaseApp().setVisible(true);
        });
    }
}
