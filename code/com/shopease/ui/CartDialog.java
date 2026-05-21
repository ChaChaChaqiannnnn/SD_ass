package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.model.Product;
import com.shopease.service.DataChangeListener;
import com.shopease.service.ShopEaseService;
import com.shopease.strategy.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Cart review — edit quantities, remove items, or continue shopping without checkout.
 */
public class CartDialog extends JDialog {
    private final ShopEaseService service;
    private final Consumer<String> onCartChanged;
    private final JPanel itemsPanel;
    private final JLabel totalLabel;
    private final JLabel hintLabel;
    private final JButton checkoutBtn;
    private final JComboBox<String> paymentCombo;
    private final DataChangeListener liveSyncListener;

    public CartDialog(JFrame parent, ShopEaseService service, Consumer<String> onCartChanged) {
        super(parent, "Your Cart", false);
        this.service = service;
        this.onCartChanged = onCartChanged;
        this.liveSyncListener = this::refreshCart;

        if (service.getCart() == null) {
            JOptionPane.showMessageDialog(parent,
                    "Cart is only available for customer accounts.",
                    "Cart", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            itemsPanel = new JPanel();
            totalLabel = new JLabel();
            hintLabel = new JLabel();
            checkoutBtn = new JButton();
            paymentCombo = new JComboBox<>();
            return;
        }

        setMinimumSize(new Dimension(560, 480));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ShopEaseUIUtils.BG_CARD);
        header.setBorder(new EmptyBorder(16, 20, 12, 20));
        JLabel title = new JLabel("Review your cart");
        title.setFont(ShopEaseUIUtils.titleFont());
        title.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        hintLabel = ShopEaseUIUtils.createMutedLabel(
                "You can remove items or change quantity — checkout is optional.");
        header.add(title, BorderLayout.NORTH);
        header.add(hintLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(ShopEaseUIUtils.BG_PAGE);
        itemsPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(ShopEaseUIUtils.BG_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232)),
                new EmptyBorder(14, 20, 16, 20)));

        totalLabel = new JLabel();
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(totalLabel);
        footer.add(Box.createVerticalStrut(10));

        JPanel payRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        payRow.setOpaque(false);
        payRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        payRow.add(new JLabel("Payment:"));
        paymentCombo = new JComboBox<>(new String[]{
                "Credit Card", "DuitNow QR", "MAE", "Touch 'n Go"
        });
        paymentCombo.setFont(ShopEaseUIUtils.bodyFont());
        payRow.add(paymentCombo);
        footer.add(payRow);
        footer.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton continueBtn = new JButton("Continue Shopping");
        ShopEaseUIUtils.styleSecondaryButton(continueBtn);
        continueBtn.addActionListener(e -> dispose());

        JButton clearBtn = new JButton("Clear Cart");
        ShopEaseUIUtils.styleSecondaryButton(clearBtn);
        clearBtn.addActionListener(e -> {
            if (service.getCart().getItems().isEmpty()) {
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove all items from your cart?",
                    "Clear cart",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                service.clearCart();
                notifyStatus();
            }
        });

        checkoutBtn = new JButton("Pay & place order");
        ShopEaseUIUtils.styleSuccessButton(checkoutBtn);
        checkoutBtn.addActionListener(e -> doCheckout());

        actions.add(continueBtn);
        actions.add(clearBtn);
        actions.add(Box.createHorizontalStrut(20));
        actions.add(checkoutBtn);
        footer.add(actions);
        add(footer, BorderLayout.SOUTH);

        service.addDataChangeListener(liveSyncListener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                service.removeDataChangeListener(liveSyncListener);
            }
        });

        refreshCart();
    }

    private void refreshCart() {
        if (service.getCart() == null) {
            return;
        }
        service.syncCartWithDatabase();
        itemsPanel.removeAll();
        java.util.List<CartItem> items = service.getCart().getItems();

        if (items.isEmpty()) {
            JLabel empty = new JLabel("<html><center>Your cart is empty.<br>"
                    + "Add products from the shop, or close this window to keep browsing.</center></html>");
            empty.setFont(ShopEaseUIUtils.bodyFont());
            empty.setForeground(ShopEaseUIUtils.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(40, 20, 40, 20));
            itemsPanel.add(empty);
            checkoutBtn.setEnabled(false);
            paymentCombo.setEnabled(false);
            totalLabel.setText("Total: RM0.00");
        } else {
            checkoutBtn.setEnabled(true);
            paymentCombo.setEnabled(true);
            for (int i = 0; i < items.size(); i++) {
                itemsPanel.add(buildItemRow(items.get(i)));
                if (i < items.size() - 1) {
                    itemsPanel.add(Box.createVerticalStrut(8));
                }
            }
            totalLabel.setText("Total: RM" + String.format("%.2f", service.getCartTotal()));
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
        pack();
    }

    private JPanel buildItemRow(CartItem item) {
        Product p = item.getProduct();
        JPanel row = new JPanel(new BorderLayout(12, 8));
        row.setBackground(ShopEaseUIUtils.BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 234, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        JPanel info = new JPanel(new GridLayout(2, 1, 2, 2));
        info.setOpaque(false);
        JLabel name = new JLabel(p.getName());
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        double lineTotal = p.getPrice() * item.getQuantity();
        JLabel sub = new JLabel(String.format("RM%.2f each  ·  line total RM%.2f",
                p.getPrice(), lineTotal));
        sub.setFont(ShopEaseUIUtils.smallFont());
        sub.setForeground(ShopEaseUIUtils.TEXT_MUTED);
        info.add(name);
        info.add(sub);
        row.add(info, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        Product fresh = service.getProductById(p.getProductId());
        int maxQty = fresh != null ? Math.max(1, fresh.getStockQuantity()) : item.getQuantity();
        SpinnerNumberModel spinModel = new SpinnerNumberModel(
                item.getQuantity(), 1, maxQty, 1);
        JSpinner qtySpinner = new JSpinner(spinModel);
        qtySpinner.setFont(ShopEaseUIUtils.bodyFont());
        String productId = p.getProductId();
        qtySpinner.addChangeListener(e -> {
            int q = (int) qtySpinner.getValue();
            if (!service.updateCartQuantity(productId, q)) {
                if (!service.getLastMessage().isEmpty()) {
                    showInlineNotice(service.getLastMessage());
                }
                refreshCart();
            } else {
                notifyStatus();
            }
        });

        JButton removeBtn = new JButton("Remove");
        ShopEaseUIUtils.styleDangerButton(removeBtn);
        removeBtn.setFont(ShopEaseUIUtils.smallFont());
        removeBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        removeBtn.addActionListener(e -> {
            service.removeFromCart(productId);
            notifyStatus();
        });

        controls.add(new JLabel("Qty:"));
        controls.add(qtySpinner);
        controls.add(removeBtn);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private void doCheckout() {
        if (service.getCart().getItems().isEmpty()) {
            showInlineNotice("Add items before checkout.");
            return;
        }

        ShopEasePaymentStrategy strategy = switch (paymentCombo.getSelectedIndex()) {
            case 0 -> new ShopEaseCreditCardStrategy();
            case 1 -> new ShopEaseDuitNowStrategy();
            case 2 -> new ShopEaseMAEStrategy();
            default -> new ShopEaseTNGStrategy();
        };

        java.util.List<CartItem> snapshot = new java.util.ArrayList<>(service.getCart().getItems());
        double finalTotal = service.getCartTotal();
        String paymentName = (String) paymentCombo.getSelectedItem();

        if (service.checkout(strategy)) {
            StringBuilder receipt = new StringBuilder();
            receipt.append("Thank you! Order placed.\n\n");
            for (CartItem item : snapshot) {
                receipt.append(" • ").append(item.getProduct().getName())
                        .append(" x").append(item.getQuantity()).append("\n");
            }
            receipt.append("\nTotal: RM").append(String.format("%.2f", finalTotal));
            receipt.append("\nPaid via: ").append(paymentName);
            receipt.append("\n\n").append(service.getLastMessage());
            JOptionPane.showMessageDialog(this, receipt, "Order complete", JOptionPane.INFORMATION_MESSAGE);
            notifyStatus();
            dispose();
        } else {
            showInlineNotice(service.getLastMessage().isEmpty()
                    ? "Checkout failed." : service.getLastMessage());
        }
    }

    private void showInlineNotice(String message) {
        hintLabel.setText(message);
        hintLabel.setForeground(ShopEaseUIUtils.DANGER);
    }

    private void notifyStatus() {
        if (onCartChanged != null) {
            onCartChanged.accept(service.getLastMessage());
        }
    }
}
