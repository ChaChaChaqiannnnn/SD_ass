package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.service.DataChangeListener;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrderHistoryDialog extends JDialog {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    private final ShopEaseService service;
    private final JTextArea historyArea;
    private final DataChangeListener liveSyncListener;

    public OrderHistoryDialog(JFrame parent, ShopEaseService service) {
        super(parent, "Order History", true);
        this.service = service;
        this.liveSyncListener = this::reloadHistory;

        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JLabel title = new JLabel("Your Past Orders");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setBorder(new EmptyBorder(15, 15, 5, 15));
        add(title, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        historyArea.setBorder(new EmptyBorder(10, 15, 10, 15));
        add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBorder(new EmptyBorder(0, 15, 15, 15));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        service.addDataChangeListener(liveSyncListener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                service.removeDataChangeListener(liveSyncListener);
            }
        });

        reloadHistory();
    }

    private void reloadHistory() {
        historyArea.setText(buildHistoryText(service.getOrderHistory()));
        historyArea.setCaretPosition(0);
    }

    private String buildHistoryText(List<Order> orders) {
        if (orders.isEmpty()) {
            return "No past orders.\nComplete a checkout to see history here.";
        }

        StringBuilder sb = new StringBuilder();
        for (Order o : orders) {
            sb.append("- Order ").append(o.getOrderId())
              .append(" | ").append(DATE_FMT.format(o.getOrderDate()))
              .append(" | RM").append(String.format("%.2f", o.getTotalAmount()))
              .append(" | ").append(o.getStatus())
              .append("\n");
            sb.append("  Items:\n");
            if (o.getItems() == null || o.getItems().isEmpty()) {
                sb.append("  - No item details recorded.\n");
            } else {
                for (CartItem item : o.getItems()) {
                    sb.append("  - ").append(item.getProduct().getName())
                      .append(" x").append(item.getQuantity())
                      .append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
