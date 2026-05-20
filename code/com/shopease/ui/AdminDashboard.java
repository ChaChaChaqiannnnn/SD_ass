package com.shopease.ui;

import com.shopease.model.Product;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JPanel {
    private ShopEaseService service;
    private Runnable onLogout;
    private DefaultListModel<Product> listModel;
    private JList<Product> productList;

    public AdminDashboard(ShopEaseService service, Runnable onLogout) {
        this.service = service;
        this.onLogout = onLogout;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(44, 62, 80)); // Dark mode for Admin

        // HEADER
        JLabel welcomeLabel = new JLabel("🔧 Admin Control Panel");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(236, 240, 241));
        add(welcomeLabel, BorderLayout.NORTH);

        // LIST
        listModel = new DefaultListModel<>();
        refreshList();

        productList = new JList<>(listModel);
        productList.setCellRenderer(new AdminProductCellRenderer());
        productList.setFont(new Font("Inter", Font.PLAIN, 14));
        productList.setBackground(new Color(52, 73, 94));
        productList.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(127, 140, 141)));
        add(scrollPane, BorderLayout.CENTER);

        // FOOTER
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        JButton restockBtn = new JButton("📦 Restock +10");
        styleButton(restockBtn, new Color(39, 174, 96));
        restockBtn.addActionListener(e -> {
            Product p = productList.getSelectedValue();
            if (p != null) {
                int newStock = p.getStockQuantity() + 10;
                service.updateProductStock(p.getProductId(), p.getName(), newStock);
                JOptionPane.showMessageDialog(this, "Restocked " + p.getName() + " to " + newStock + "!");
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "Select a product first.");
            }
        });

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> {
            service.logout();
            onLogout.run();
        });

        bottomPanel.add(restockBtn);
        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void refreshList() {
        listModel.clear();
        service.getAllProducts().forEach(listModel::addElement);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }
    
    private static class AdminProductCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product) {
                Product p = (Product) value;
                String status = p.getStockQuantity() < 5 ? "[!] LOW STOCK" : "✅ OK";
                setText(p.getName() + "  |  Current Stock: " + p.getStockQuantity() + "  |  " + status);
                setBorder(new EmptyBorder(10, 10, 10, 10));
                if (p.getStockQuantity() < 5) {
                    setForeground(new Color(231, 76, 60)); // Red text for low stock
                } else if (!isSelected) {
                    setForeground(Color.WHITE);
                }
            }
            return this;
        }
    }
}
