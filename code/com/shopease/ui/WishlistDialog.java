package com.shopease.ui;

import com.shopease.model.Product;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseUiRefreshObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WishlistDialog extends JDialog {
    private final ShopEaseService service;
    private final JPanel listPanel;
    private final ShopEaseInventoryObserver syncObserver;

    public WishlistDialog(JFrame parent, ShopEaseService service) {
        super(parent, "My Wishlist", false);
        this.service = service;
        this.syncObserver = new ShopEaseUiRefreshObserver(this::refreshList);

        setSize(540, 450);
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 0, 20));
        JLabel title = new JLabel("Saved for later");
        title.setFont(ShopEaseUIUtils.titleFont());
        JLabel subtitle = ShopEaseUIUtils.createMutedLabel(
                "You will be notified at sign-in when saved items are restocked.");
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(8, 16, 8, 16));
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        ShopEaseUIUtils.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 16, 16, 16));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        service.attachObserver(syncObserver);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                service.detachObserver(syncObserver);
            }
        });

        refreshList();
    }

    private void refreshList() {
        listPanel.removeAll();
        java.util.List<Product> items = service.getWishlistProducts();
        if (items.isEmpty()) {
            JLabel empty = new JLabel("<html><center>Your wishlist is empty.<br>"
                    + "Use ♥ Save on products while browsing.</center></html>");
            empty.setFont(ShopEaseUIUtils.bodyFont());
            empty.setForeground(ShopEaseUIUtils.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(40, 20, 40, 20));
            listPanel.add(empty);
        } else {
            for (Product p : items) {
                listPanel.add(buildRow(p));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildRow(Product p) {
        JPanel row = new JPanel(new BorderLayout(10, 8));
        row.setBackground(ShopEaseUIUtils.BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232)),
                new EmptyBorder(12, 14, 12, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel info = new JLabel("<html><b>" + p.getName() + "</b><br>RM "
                + String.format("%.2f", p.getPrice()) + " · Stock: " + p.getStockQuantity() + "</html>");
        info.setFont(ShopEaseUIUtils.bodyFont());
        row.add(info, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        if (p.getStockQuantity() > 0) {
            JButton toCart = new JButton("Add to cart");
            ShopEaseUIUtils.styleSuccessButton(toCart);
            toCart.addActionListener(e -> {
                if (service.moveWishlistItemToCart(p.getProductId(), 1)) {
                    refreshList();
                } else {
                    JOptionPane.showMessageDialog(this, service.getLastMessage(), "Wishlist",
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            actions.add(toCart);
        }

        JButton remove = new JButton("Remove");
        ShopEaseUIUtils.styleDangerButton(remove);
        remove.setFont(ShopEaseUIUtils.smallFont());
        remove.addActionListener(e -> {
            service.removeFromWishlist(p.getProductId());
            refreshList();
        });
        actions.add(remove);
        row.add(actions, BorderLayout.EAST);
        return row;
    }
}
