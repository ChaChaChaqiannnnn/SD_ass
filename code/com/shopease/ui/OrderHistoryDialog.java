package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseUiRefreshObserver;
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
    private final ShopEaseInventoryObserver uiRefreshObserver;

    public OrderHistoryDialog(JFrame parent, ShopEaseService service) {
        super(parent, "Order History", true);
        this.service = service;
        this.uiRefreshObserver = new ShopEaseUiRefreshObserver(this::reloadHistory);

        setMinimumSize(new Dimension(580, 440));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 0, 20));
        JLabel title = new JLabel("Your past orders");
        title.setFont(ShopEaseUIUtils.titleFont());
        JLabel subtitle = ShopEaseUIUtils.createMutedLabel("Order IDs use date + daily sequence (dd/MM/yy-###).");
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(ShopEaseUIUtils.bodyFont());
        historyArea.setBackground(ShopEaseUIUtils.BG_CARD);
        historyArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 232)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(8, 20, 8, 20));
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 16, 16, 16));
        JButton closeBtn = new JButton("Close");
        ShopEaseUIUtils.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        service.attachObserver(uiRefreshObserver);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                service.detachObserver(uiRefreshObserver);
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
            return "No orders yet.\n\nAdd items to your cart and checkout to see history here.";
        }

        StringBuilder sb = new StringBuilder();
        for (Order o : orders) {
            sb.append("Order ").append(o.getOrderId())
              .append("\n  Date: ").append(DATE_FMT.format(o.getOrderDate()))
              .append("  ·  Total: RM ").append(String.format("%.2f", o.getTotalAmount()))
              .append("  ·  ").append(o.getStatus())
              .append("\n  Items:\n");
            if (o.getItems() == null || o.getItems().isEmpty()) {
                sb.append("    (no line items recorded)\n");
            } else {
                for (CartItem item : o.getItems()) {
                    sb.append("    • ").append(item.getProduct().getName())
                      .append(" × ").append(item.getQuantity())
                      .append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
