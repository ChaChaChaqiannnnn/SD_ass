package com.shopease.ui;

import com.shopease.model.Product;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JPanel {
    private ShopEaseService service;
    private Runnable onLogout;
    private JPanel productsPanel;
    private List<Product> allProducts;

    public CustomerDashboard(ShopEaseService service, Runnable onLogout) {
        this.service = service;
        this.onLogout = onLogout;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250)); // Modern light background

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Hello, " + service.getCurrentUser().getName() + " 👋");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));
        
        JButton cartBtn = new JButton("🛒 View Cart & Checkout");
        styleButton(cartBtn, new Color(52, 152, 219));
        cartBtn.addActionListener(e -> showCartDialog());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(cartBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // SEARCH BAR
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Inter", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createTitledBorder("Search Products..."));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText().toLowerCase();
                renderProducts(text);
            }
        });
        centerPanel.add(searchField, BorderLayout.NORTH);

        // PRODUCTS GRID
        productsPanel = new JPanel(new GridLayout(0, 1, 15, 15));
        productsPanel.setOpaque(false);
        productsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // Responsive Layout Listener
        scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = scrollPane.getWidth();
                int cols = Math.max(1, width / 300); // 300px min width per card
                productsPanel.setLayout(new GridLayout(0, cols, 15, 15));
                productsPanel.revalidate();
            }
        });
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // FOOTER
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);
        
        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> {
            service.logout();
            onLogout.run();
        });

        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initial Load
        allProducts = service.getAllProducts();
        renderProducts("");
    }
    
    private void renderProducts(String filterText) {
        productsPanel.removeAll();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(filterText)) {
                productsPanel.add(createProductCard(p));
            }
        }
        productsPanel.revalidate();
        productsPanel.repaint();
    }
    
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(new Font("Inter", Font.BOLD, 16));
        
        JLabel priceLabel = new JLabel("RM " + String.format("%.2f", p.getPrice()) + " | Stock: " + p.getStockQuantity());
        priceLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(127, 140, 141));
        
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);
        
        JButton addBtn = new JButton("+ Add to Cart");
        if (p.getStockQuantity() > 0) {
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, p.getStockQuantity(), 1);
            JSpinner qtySpinner = new JSpinner(spinnerModel);
            qtySpinner.setFont(new Font("Inter", Font.PLAIN, 14));

            styleButton(addBtn, new Color(46, 204, 113));
            addBtn.addActionListener(e -> {
                int qty = (int) qtySpinner.getValue();
                service.addToCart(p, qty);
                JOptionPane.showMessageDialog(this, qty + "x " + p.getName() + " added to cart!");
            });
            
            JLabel qtyLabel = new JLabel("Qty:");
            qtyLabel.setFont(new Font("Inter", Font.PLAIN, 14));
            actionPanel.add(qtyLabel);
            actionPanel.add(qtySpinner);
            actionPanel.add(addBtn);
        } else {
            styleButton(addBtn, new Color(189, 195, 199));
            addBtn.setText("Out of Stock");
            addBtn.setEnabled(false);
            actionPanel.add(addBtn);
        }
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private void showCartDialog() {
        CartDialog dialog = new CartDialog((JFrame) SwingUtilities.getWindowAncestor(this), service);
        dialog.pack(); // Fixes layout cutoff issues
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        // Refresh product list stock after potential checkout
        allProducts = service.getAllProducts();
        renderProducts("");
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
