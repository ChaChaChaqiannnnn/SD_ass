package com.shopease.ui;

import com.shopease.model.InventoryStockStatus;
import com.shopease.model.Product;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseDataChangeRefreshObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Customer wishlist — saved products with restock notifications on login.
 * <p>
 * Singleton Pattern — reads/writes via {@link ShopEaseService} which uses
 * {@link com.shopease.singleton.ShopEaseWishlistSingleton} per logged-in customer.
 * Observer Pattern — {@link com.shopease.observer.ShopEaseDataChangeRefreshObserver}
 * refreshes the list when stock or wishlist data changes.
 */
public class WishlistDialog extends JDialog {
    private final ShopEaseService service;
    private final JPanel listPanel;
    private final JLabel countLabel;
    private final ShopEaseInventoryObserver syncObserver;

    public WishlistDialog(JFrame parent, ShopEaseService service) {
        super(parent, "My Wishlist", false);
        this.service = service;
        this.syncObserver = new ShopEaseDataChangeRefreshObserver(this::refreshList);

        setSize(560, 480);
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout(0, 0));

        // ── Header (left-aligned, matches Cart / Profile dialogs) ───────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ShopEaseUIUtils.BG_CARD);
        header.setBorder(new EmptyBorder(18, 24, 14, 24));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("My wishlist");
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

        // ── Scrollable list ─────────────────────────────────────────────────────
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(ShopEaseUIUtils.BG_PAGE);
        listPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────────────────────────
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

        int count = items.size();
        countLabel.setText(count == 0 ? " " : count == 1 ? "1 item" : count + " items");

        if (items.isEmpty()) {
            listPanel.add(buildEmptyState());
        } else {
            for (int i = 0; i < items.size(); i++) {
                listPanel.add(buildRow(items.get(i)));
                if (i < items.size() - 1) {
                    listPanel.add(Box.createVerticalStrut(10));
                }
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    /** Empty state — copy lives inside the wishlist box only. */
    private JPanel buildEmptyState() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232), 1, true),
                new EmptyBorder(28, 24, 28, 24)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel hint = ShopEaseUIUtils.createMutedLabel(
                "Things you want to grab later. We'll let you know when they're back in stock.");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Nothing here yet");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        heading.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(hint);
        card.add(Box.createVerticalStrut(10));
        card.add(heading);
        return card;
    }

    private JPanel buildRow(Product p) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(ShopEaseUIUtils.BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel name = new JLabel(p.getName());
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        name.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel price = new JLabel("RM " + String.format("%.2f", p.getPrice()));
        price.setFont(ShopEaseUIUtils.bodyFont());
        price.setForeground(ShopEaseUIUtils.TEXT_MUTED);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);

        int stock = p.getStockQuantity();
        JLabel stockLabel = new JLabel(stockStatusText(stock));
        stockLabel.setFont(ShopEaseUIUtils.smallFont());
        stockLabel.setForeground(stockColor(stock));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(price);
        info.add(Box.createVerticalStrut(2));
        info.add(stockLabel);
        row.add(info, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        if (stock > 0) {
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
        } else {
            JLabel wait = ShopEaseUIUtils.createMutedLabel("Sold out for now");
            wait.setFont(ShopEaseUIUtils.smallFont());
            actions.add(wait);
        }

        JButton remove = new JButton("Remove");
        ShopEaseUIUtils.styleDangerButton(remove);
        remove.setFont(ShopEaseUIUtils.smallFont());
        remove.setBorder(new EmptyBorder(6, 12, 6, 12));
        remove.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove \"" + p.getName() + "\" from your wishlist?",
                    "Remove from wishlist",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                service.removeFromWishlist(p.getProductId());
                refreshList();
            }
        });
        actions.add(remove);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private static String stockStatusText(int stock) {
        if (InventoryStockStatus.isOutOfStock(stock)) {
            return "Sold out for now";
        }
        if (InventoryStockStatus.isLowStock(stock)) {
            return "Only " + stock + " left";
        }
        return stock + " in stock";
    }

    private static Color stockColor(int stock) {
        if (InventoryStockStatus.isOutOfStock(stock)) {
            return ShopEaseUIUtils.DANGER;
        }
        if (InventoryStockStatus.isLowStock(stock)) {
            return ShopEaseUIUtils.WARNING.darker();
        }
        return ShopEaseUIUtils.SUCCESS;
    }
}
