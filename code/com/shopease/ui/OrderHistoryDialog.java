package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.observer.Observer;
import com.shopease.observer.ShopEaseDataChangeRefreshObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Read-only order history — plain labels (not a text box), no internal order IDs shown.
 * <p>
 * Observer Pattern — {@link com.shopease.observer.ShopEaseDataChangeRefreshObserver}
 * reloads orders when checkout or admin changes fire {@code DATA_CHANGED}.
 */
public class OrderHistoryDialog extends JDialog {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    private final ShopEaseService service;
    private final JPanel ordersPanel;
    private final JLabel countLabel;
    private final Observer uiRefreshObserver;

    public OrderHistoryDialog(JFrame parent, ShopEaseService service) {
        super(parent, "Order History", true);
        this.service = service;
        this.uiRefreshObserver = new ShopEaseDataChangeRefreshObserver(this::reloadHistory);

        setSize(580, 480);
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ShopEaseUIUtils.BG_CARD);
        header.setBorder(new EmptyBorder(18, 24, 14, 24));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Your past orders");
        title.setFont(ShopEaseUIUtils.titleFont());
        title.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        countLabel = ShopEaseUIUtils.createMutedLabel(" ");
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(countLabel);
        header.add(titleBlock, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(ShopEaseUIUtils.BG_PAGE);
        ordersPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JScrollPane scroll = new JScrollPane(ordersPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setBackground(ShopEaseUIUtils.BG_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232)),
                new EmptyBorder(12, 20, 14, 20)));
        JButton closeBtn = new JButton("Close");
        ShopEaseUIUtils.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);

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
        ordersPanel.removeAll();
        List<Order> orders = service.getOrderHistory();

        int count = orders.size();
        countLabel.setText(count == 0
                ? "No orders yet"
                : count == 1 ? "1 order" : count + " orders");

        if (orders.isEmpty()) {
            ordersPanel.add(buildEmptyState());
        } else {
            for (int i = 0; i < orders.size(); i++) {
                ordersPanel.add(buildOrderCard(orders.get(i)));
                if (i < orders.size() - 1) {
                    ordersPanel.add(Box.createVerticalStrut(10));
                }
            }
        }
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private JPanel buildEmptyState() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232), 1, true),
                new EmptyBorder(28, 24, 28, 24)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel heading = new JLabel("No orders yet");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        heading.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel body = new JLabel("<html><body style='width:380px;line-height:1.5'>"
                + "When you checkout, your purchases will show up here."
                + "</body></html>");
        body.setFont(ShopEaseUIUtils.bodyFont());
        body.setForeground(ShopEaseUIUtils.TEXT_MUTED);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(8));
        card.add(body);
        return card;
    }

    private JPanel buildOrderCard(Order order) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232), 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel dateLine = new JLabel(DATE_FMT.format(order.getOrderDate()));
        dateLine.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        dateLine.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        dateLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summary = new JLabel(String.format("RM %.2f  ·  %s",
                order.getTotalAmount(), order.getStatus()));
        summary.setFont(ShopEaseUIUtils.bodyFont());
        summary.setForeground(ShopEaseUIUtils.TEXT_MUTED);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel itemsHeading = new JLabel("Items");
        itemsHeading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        itemsHeading.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        itemsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(dateLine);
        card.add(Box.createVerticalStrut(6));
        card.add(summary);
        card.add(Box.createVerticalStrut(12));
        card.add(itemsHeading);
        card.add(Box.createVerticalStrut(6));

        if (order.getItems() == null || order.getItems().isEmpty()) {
            JLabel none = ShopEaseUIUtils.createMutedLabel("No item details recorded.");
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(none);
        } else {
            for (CartItem item : order.getItems()) {
                JLabel line = new JLabel("  •  " + item.getProduct().getName()
                        + "  ×  " + item.getQuantity());
                line.setFont(ShopEaseUIUtils.bodyFont());
                line.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(line);
                card.add(Box.createVerticalStrut(2));
            }
        }
        return card;
    }
}
