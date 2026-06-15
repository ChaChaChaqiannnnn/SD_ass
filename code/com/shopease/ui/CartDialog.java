package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.model.Product;
import com.shopease.observer.Observer;
import com.shopease.observer.ShopEaseDataChangeRefreshObserver;
import com.shopease.service.ShopEaseService;
import com.shopease.strategy.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Cart popup — review items, pick payment method (Strategy), checkout, then show receipt.
 * Also attaches DataChangeRefreshObserver so the cart list updates when stock changes elsewhere.
 */
public class CartDialog extends JDialog {
    private static final String DEFAULT_HINT =
            "You can remove items or change quantity — checkout is optional.";

    /** GoF Strategy — client-visible payment labels; each maps to a ConcreteStrategy. */
    private static final String PAY_CREDIT_CARD = "Credit Card";
    private static final String PAY_DUIT_NOW = "DuitNow QR";
    private static final String PAY_MAE = "MAE";
    private static final String PAY_TNG = "Touch 'n Go";
    private static final String[] PAYMENT_METHODS = {
            PAY_CREDIT_CARD, PAY_DUIT_NOW, PAY_MAE, PAY_TNG
    };

    private final ShopEaseService service;
    private final Consumer<String> onCartChanged;
    private final JPanel itemsPanel;
    private final JLabel totalLabel;
    private final JLabel hintLabel;
    private final JButton checkoutBtn;
    private final JComboBox<String> paymentCombo;
    private final Observer uiRefreshObserver;

    public CartDialog(JFrame parent, ShopEaseService service, Consumer<String> onCartChanged) {
        super(parent, "Your Cart", false);
        this.service = service;
        this.onCartChanged = onCartChanged;
        this.uiRefreshObserver = new ShopEaseDataChangeRefreshObserver(this::refreshCart);

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

        setSize(580, 520);
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
        hintLabel = ShopEaseUIUtils.createMutedLabel(DEFAULT_HINT);
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
        // GoF Strategy — client picks ConcreteStrategy from dropdown
        paymentCombo = new JComboBox<>(PAYMENT_METHODS);
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

        service.attachObserver(uiRefreshObserver);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                service.detachObserver(uiRefreshObserver);
            }
        });

        refreshCart();
    }

    private void refreshCart() {
        if (service.getCart() == null) {
            return;
        }
        hintLabel.setText(DEFAULT_HINT);
        hintLabel.setForeground(ShopEaseUIUtils.TEXT_MUTED);
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
            int confirm = JOptionPane.showConfirmDialog(
                    CartDialog.this,
                    "Remove \"" + p.getName() + "\" from your cart?",
                    "Remove item",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                service.removeFromCart(productId);
                notifyStatus();
            }
        });

        controls.add(new JLabel("Qty:"));
        controls.add(qtySpinner);
        controls.add(removeBtn);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    /** Checkout button — Strategy pattern picks payment, then shows ReceiptDialog on success. */
    private void doCheckout() {
        if (service.getCart().getItems().isEmpty()) {
            showInlineNotice("Add items before checkout.");
            return;
        }

        // GoF Strategy — client selects ConcreteStrategy, Context runs it in service.checkout()
        final String paymentName = (String) paymentCombo.getSelectedItem();
        final ShopEasePaymentStrategy strategy = paymentStrategyFor(paymentName);

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Place order for <b>RM" + String.format("%.2f", service.getCartTotal())
                        + "</b> using <b>" + paymentName + "</b>?</html>",
                "Confirm checkout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Snapshot cart before the async call (cart is cleared on success)
        final java.util.List<CartItem> snapshot =
                new java.util.ArrayList<>(service.getCart().getItems());
        final double finalTotal = service.getCartTotal();

        // SwingWorker — moves SQLite write off the EDT to prevent Windows freeze
        checkoutBtn.setEnabled(false);
        checkoutBtn.setText("Processing…");
        paymentCombo.setEnabled(false);

        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                // Step 2 (Strategy) — service runs payment + saves order + updates stock
                return service.checkout(strategy);
            }

            @Override
            protected void done() {
                checkoutBtn.setEnabled(true);
                checkoutBtn.setText("Pay & place order");
                paymentCombo.setEnabled(true);
                try {
                    if (get()) {
                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(CartDialog.this);
                        String customerName = service.getCurrentUser() != null
                                ? service.getCurrentUser().getName() : "";
                        ReceiptDialog receipt = new ReceiptDialog(
                                parentFrame,
                                service.getLastOrderId(),
                                customerName,
                                snapshot,
                                finalTotal,
                                paymentName);
                        receipt.setVisible(true);
                        notifyStatus();
                        dispose();
                    } else {
                        showInlineNotice(service.getLastMessage().isEmpty()
                                ? "Checkout failed." : service.getLastMessage());
                    }
                } catch (Exception ex) {
                    showInlineNotice("An unexpected error occurred. Please try again.");
                }
            }
        }.execute();
    }

    /** GoF Strategy — client chooses which ConcreteStrategy to pass to the Context. */
    private static ShopEasePaymentStrategy paymentStrategyFor(String paymentName) {
        if (paymentName == null) {
            return new ShopEaseCreditCardStrategy();
        }
        return switch (paymentName) {
            case PAY_DUIT_NOW -> new ShopEaseDuitNowStrategy();
            case PAY_MAE -> new ShopEaseMAEStrategy();
            case PAY_TNG -> new ShopEaseTNGStrategy();
            default -> new ShopEaseCreditCardStrategy();
        };
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
