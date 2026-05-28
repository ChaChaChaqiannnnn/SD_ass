package com.shopease.ui;

import com.shopease.model.CartItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Thank-you receipt shown after a successful checkout in CartDialog.
 * Displays order ID (dd/MM/yy-NNN format), items, total, and payment method used.
 */
public class ReceiptDialog extends JDialog {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    public ReceiptDialog(JFrame parent, String orderId, String customerName,
                         List<CartItem> items, double total, String paymentMethod) {
        super(parent, "Payment receipt", true);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 232)),
                new EmptyBorder(24, 28, 24, 28)));

        JLabel thankYou = new JLabel("Thank you!");
        thankYou.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        thankYou.setForeground(ShopEaseUIUtils.SUCCESS);
        thankYou.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(thankYou);

        JLabel subtitle = new JLabel("Your payment was successful.");
        subtitle.setFont(ShopEaseUIUtils.bodyFont());
        subtitle.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);

        JLabel message = new JLabel("<html>We appreciate your purchase at <b>ShopEase Malaysia</b>.<br>"
                + "Your order is confirmed and saved to your order history.</html>");
        message.setFont(ShopEaseUIUtils.smallFont());
        message.setForeground(ShopEaseUIUtils.TEXT_MUTED);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(message);
        card.add(Box.createVerticalStrut(16));

        JPanel meta = new JPanel(new GridLayout(0, 1, 0, 6));
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(236, 240, 241)),
                new EmptyBorder(12, 14, 12, 14)));
        meta.add(metaLine("Order ID", orderId));
        meta.add(metaLine("Customer", customerName != null ? customerName : "—"));
        meta.add(metaLine("Date", DATE_FMT.format(new Date())));
        meta.add(metaLine("Payment", paymentMethod));
        card.add(meta);
        card.add(Box.createVerticalStrut(16));

        JLabel itemsTitle = new JLabel("Items purchased");
        itemsTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        itemsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(itemsTitle);
        card.add(Box.createVerticalStrut(8));

        StringBuilder itemsHtml = new StringBuilder("<html><body style='width:320px'>");
        for (CartItem item : items) {
            double line = item.getProduct().getPrice() * item.getQuantity();
            itemsHtml.append("• ").append(item.getProduct().getName())
                    .append(" &times; ").append(item.getQuantity())
                    .append(" — RM ").append(String.format("%.2f", line))
                    .append("<br>");
        }
        itemsHtml.append("</body></html>");
        JLabel itemsList = new JLabel(itemsHtml.toString());
        itemsList.setFont(ShopEaseUIUtils.bodyFont());
        itemsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(itemsList);
        card.add(Box.createVerticalStrut(12));

        JLabel totalLabel = new JLabel("Total paid: RM " + String.format("%.2f", total));
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        totalLabel.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(totalLabel);
        card.add(Box.createVerticalStrut(8));

        JLabel closing = ShopEaseUIUtils.createMutedLabel("See you again soon!");
        closing.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(closing);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 16, 8, 16));
        wrap.add(card);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 16, 16, 16));
        JButton okBtn = new JButton("Done");
        ShopEaseUIUtils.styleSuccessButton(okBtn);
        okBtn.addActionListener(e -> dispose());
        bottom.add(okBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private static JLabel metaLine(String label, String value) {
        return new JLabel("<html><span style='color:#6c757d'>" + label + ":</span> <b>" + value + "</b></html>");
    }
}
