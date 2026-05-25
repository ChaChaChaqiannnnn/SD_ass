package com.shopease.ui;

import com.shopease.model.Product;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseUiRefreshObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JPanel {
    private final ShopEaseService service;
    private final Runnable onLogout;
    private JPanel productsPanel;
    private List<Product> allProducts;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private JButton cartBtn;
    private String lastSearchFilter = "";
    private final ShopEaseInventoryObserver uiRefreshObserver;

    public CustomerDashboard(ShopEaseService service, Runnable onLogout) {
        this.service = service;
        this.uiRefreshObserver = new ShopEaseUiRefreshObserver(this::refreshAll);
        service.attachObserver(uiRefreshObserver);
        this.onLogout = () -> {
            service.detachObserver(uiRefreshObserver);
            onLogout.run();
        };
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setBackground(ShopEaseUIUtils.BG_PAGE);

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleLabel = new JLabel("Hello, " + service.getCurrentUser().getName());
        titleLabel.setFont(ShopEaseUIUtils.titleFont());
        titleLabel.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        JLabel subtitle = ShopEaseUIUtils.createMutedLabel("Browse products — open your cart anytime to edit or checkout.");
        titleBlock.add(titleLabel);
        titleBlock.add(subtitle);

        JButton profileBtn = new JButton("Profile settings");
        ShopEaseUIUtils.styleSecondaryButton(profileBtn);
        profileBtn.setToolTipText("Change your name, email, or password");
        profileBtn.addActionListener(e -> showProfileDialog());

        JButton wishlistBtn = new JButton("Wishlist");
        ShopEaseUIUtils.styleSecondaryButton(wishlistBtn);
        wishlistBtn.addActionListener(e -> showWishlistDialog());

        JButton ordersBtn = new JButton("Orders");
        ShopEaseUIUtils.styleSecondaryButton(ordersBtn);
        ordersBtn.addActionListener(e -> showOrderHistoryDialog());

        cartBtn = new JButton("View Cart");
        ShopEaseUIUtils.stylePrimaryButton(cartBtn);
        cartBtn.addActionListener(e -> showCartDialog());

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        headerActions.setOpaque(false);
        headerActions.add(profileBtn);
        headerActions.add(wishlistBtn);
        headerActions.add(ordersBtn);
        headerActions.add(cartBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(headerActions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        JTextField searchField = new JTextField();
        searchField.setFont(ShopEaseUIUtils.bodyFont());
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232)),
                new EmptyBorder(10, 12, 10, 12)));
        searchField.setToolTipText("Search by product name");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(searchField); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(searchField); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(searchField); }
        });
        centerPanel.add(searchField, BorderLayout.NORTH);

        productsPanel = new JPanel(new GridLayout(0, 1, 12, 12));
        productsPanel.setOpaque(false);
        productsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = scrollPane.getWidth();
                int cols = Math.max(1, width / 320);
                productsPanel.setLayout(new GridLayout(0, cols, 12, 12));
                productsPanel.revalidate();
            }
        });

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        statusLabel = ShopEaseUIUtils.createMutedLabel("Tip: Added items stay in your cart until you remove them or checkout.");
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        ShopEaseUIUtils.styleDangerButton(logoutBtn);
        logoutBtn.addActionListener(e -> {
            service.logout();
            onLogout.run();
        });
        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoutWrap.setOpaque(false);
        logoutWrap.add(logoutBtn);
        bottomPanel.add(logoutWrap, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshAll();
    }

    private void refreshAll() {
        allProducts = service.getAllProducts();
        renderProducts(lastSearchFilter);
        updateCartBadge();
    }

    private void applyFilter(JTextField searchField) {
        lastSearchFilter = searchField.getText().toLowerCase();
        renderProducts(lastSearchFilter);
    }

    private void renderProducts(String filterText) {
        productsPanel.removeAll();
        int shown = 0;
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(filterText)) {
                productsPanel.add(createProductCard(p));
                shown++;
            }
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No products match your search.");
            empty.setFont(ShopEaseUIUtils.bodyFont());
            empty.setForeground(ShopEaseUIUtils.TEXT_MUTED);
            empty.setBorder(new EmptyBorder(24, 8, 24, 8));
            productsPanel.add(empty);
        }
        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232), 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        String stockText = p.getStockQuantity() > 0
                ? "RM " + String.format("%.2f", p.getPrice()) + "  ·  " + p.getStockQuantity() + " in stock"
                : "RM " + String.format("%.2f", p.getPrice()) + "  ·  Out of stock";
        JLabel priceLabel = new JLabel(stockText);
        priceLabel.setFont(ShopEaseUIUtils.smallFont());
        priceLabel.setForeground(ShopEaseUIUtils.TEXT_MUTED);

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        boolean inWishlist = service.isInWishlist(p.getProductId());
        JButton wishBtn = new JButton(inWishlist ? "♥ Saved" : "♥ Save");
        if (inWishlist) {
            ShopEaseUIUtils.styleSecondaryButton(wishBtn);
        } else {
            ShopEaseUIUtils.styleButton(wishBtn, new Color(155, 89, 182));
        }
        // attaching the observer so the dashboard updates automatically if cart or wishlist changes
        wishBtn.addActionListener(e -> {
            wishBtn.setEnabled(false);
            // SwingWorker — wishlist DB read/write off the EDT
            new javax.swing.SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    // interacting with the wishlist through our service layer
                    if (service.isInWishlist(p.getProductId())) {
                        service.removeFromWishlist(p.getProductId());
                    } else {
                        service.addToWishlist(p.getProductId());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    wishBtn.setEnabled(true);
                    showStatus(service.getLastMessage(), false);
                    renderProducts(lastSearchFilter);
                }
            }.execute();
        });
        actionPanel.add(wishBtn);

        JButton addBtn = new JButton("Add to cart");
        if (p.getStockQuantity() > 0) {
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, p.getStockQuantity(), 1);
            JSpinner qtySpinner = new JSpinner(spinnerModel);
            qtySpinner.setFont(ShopEaseUIUtils.bodyFont());

            ShopEaseUIUtils.styleSuccessButton(addBtn);
            addBtn.addActionListener(e -> {
                // SwingWorker — Singleton cart + DB write off the EDT
                addBtn.setEnabled(false);
                addBtn.setText("Adding…");
                final int qty = (int) qtySpinner.getValue();
                new javax.swing.SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        Product latest = service.getProductById(p.getProductId());
                        if (latest == null) {
                            return null;
                        }
                        // updating the single cart instance in the background
                        service.addToCart(latest, qty);
                        return null;
                    }

                    @Override
                    protected void done() {
                        addBtn.setEnabled(true);
                        addBtn.setText("Add to cart");
                        String msg = service.getLastMessage();
                        boolean error = msg != null && (msg.contains("exceed") || msg.contains("stock")
                                || msg.contains("unavailable") || msg.contains("no longer"));
                        showStatus(msg != null ? msg : "", error);
                    }
                }.execute();
            });

            JLabel qtyLabel = new JLabel("Qty");
            qtyLabel.setFont(ShopEaseUIUtils.smallFont());
            actionPanel.add(qtyLabel);
            actionPanel.add(qtySpinner);
            actionPanel.add(addBtn);
        } else {
            addBtn.setEnabled(false);
            addBtn.setText("Unavailable");
            ShopEaseUIUtils.styleSecondaryButton(addBtn);
            actionPanel.add(addBtn);
        }

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? ShopEaseUIUtils.DANGER : ShopEaseUIUtils.TEXT_MUTED);
    }

    private void updateCartBadge() {
        int n = service.getCartItemCount();
        cartBtn.setText(n > 0 ? "View Cart (" + n + ")" : "View Cart");
    }

    private void showProfileDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ProfileDialog dialog = new ProfileDialog(parent, service, this::refreshHeader);
        dialog.setVisible(true);
    }

    private void refreshHeader() {
        if (service.getCurrentUser() != null) {
            titleLabel.setText("Hello, " + service.getCurrentUser().getName());
        }
    }

    private void showWishlistDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        WishlistDialog dialog = new WishlistDialog(parent, service);
        dialog.setVisible(true);
    }

    private void showOrderHistoryDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        OrderHistoryDialog dialog = new OrderHistoryDialog(parent, service);
        dialog.setVisible(true);
    }

    private void showCartDialog() {
        if (service.getCart() == null) {
            showStatus("Cart is only available for customer accounts.", true);
            return;
        }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        CartDialog dialog = new CartDialog(parent, service, msg -> {
            if (msg != null && !msg.isEmpty()) {
                showStatus(msg, false);
            }
        });
        dialog.setVisible(true);
    }
}
