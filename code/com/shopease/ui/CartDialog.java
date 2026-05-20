package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.service.ShopEaseService;
import com.shopease.strategy.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CartDialog extends JDialog {
    private ShopEaseService service;
    
    public CartDialog(JFrame parent, ShopEaseService service) {
        super(parent, "Your Shopping Cart", true);
        this.service = service;
        setMinimumSize(new Dimension(500, 400));
        setPreferredSize(new Dimension(500, 400));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        List<CartItem> items = service.getCart().getItems();
        
        DefaultListModel<String> model = new DefaultListModel<>();
        double total = 0;
        for (CartItem item : items) {
            double subtotal = item.getProduct().getPrice() * item.getQuantity();
            model.addElement(item.getProduct().getName() + " x" + item.getQuantity() + " - RM" + String.format("%.2f", subtotal));
            total += subtotal;
        }
        final double finalTotal = total;

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Inter", Font.PLAIN, 14));
        list.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.setOpaque(false);
        
        JLabel totalLabel = new JLabel("Total: RM" + String.format("%.2f", total));
        totalLabel.setFont(new Font("Inter", Font.BOLD, 16));
        bottomPanel.add(totalLabel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        JButton checkoutBtn = new JButton("Proceed to Checkout");
        checkoutBtn.setBackground(new Color(46, 204, 113));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFont(new Font("Inter", Font.BOLD, 14));
        checkoutBtn.setOpaque(true);
        checkoutBtn.setBorderPainted(false);
        checkoutBtn.addActionListener(e -> {
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
                return;
            }
            
            String[] options = {"Credit Card", "DuitNow QR", "MAE", "Touch 'n Go"};
            int choice = JOptionPane.showOptionDialog(this, 
                    "Select Payment Method", 
                    "Checkout Strategy",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                    null, options, options[0]);

            if (choice >= 0) {
                ShopEasePaymentStrategy strategy;
                switch (choice) {
                    case 0: strategy = new ShopEaseCreditCardStrategy(); break;
                    case 1: strategy = new ShopEaseDuitNowStrategy(); break;
                    case 2: strategy = new ShopEaseMAEStrategy(); break;
                    default: strategy = new ShopEaseTNGStrategy(); break;
                }
                
                // Build visual Receipt details BEFORE clearing cart in service
                StringBuilder receipt = new StringBuilder();
                receipt.append("======== SHOPEASE RECEIPT ========\n\n");
                receipt.append("Thank you for your purchase!\n\n");
                receipt.append("Items:\n");
                for (com.shopease.model.CartItem item : items) {
                    double subtotal = item.getProduct().getPrice() * item.getQuantity();
                    receipt.append(" • ").append(item.getProduct().getName())
                           .append(" x").append(item.getQuantity())
                           .append(" - RM").append(String.format("%.2f", subtotal))
                           .append("\n");
                }
                receipt.append("\n----------------------------------\n");
                receipt.append("Total Paid: RM").append(String.format("%.2f", finalTotal));
                receipt.append("\nPayment Via: ").append(options[choice]);
                receipt.append("\n==================================\n\n");
                receipt.append("Your order has been placed. Returning to shop.");

                if (service.checkout(strategy)) {
                    JOptionPane.showMessageDialog(this, receipt.toString(), "Payment Successful & Receipt", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close cart dialog to return to the main dashboard
                } else {
                    JOptionPane.showMessageDialog(this, "Checkout failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        
        actionPanel.add(closeBtn);
        actionPanel.add(checkoutBtn);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
