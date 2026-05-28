package com.shopease.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Shared styling for consistent, low-fatigue customer UI. */
public final class ShopEaseUIUtils {
    public static final Color BG_PAGE = new Color(245, 247, 250);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    public static final Color TEXT_MUTED = new Color(108, 117, 125);
    public static final Color ACCENT = new Color(52, 152, 219);
    public static final Color SUCCESS = new Color(46, 204, 113);
    public static final Color DANGER = new Color(231, 76, 60);
    public static final Color WARNING = new Color(241, 196, 15);

    private ShopEaseUIUtils() {}

    public static Font titleFont() {
        return new Font(Font.SANS_SERIF, Font.BOLD, 22);
    }

    public static Font bodyFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    }

    public static Font smallFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    public static void stylePrimaryButton(JButton btn) {
        styleButton(btn, ACCENT);
    }

    public static void styleSuccessButton(JButton btn) {
        styleButton(btn, SUCCESS);
    }

    public static void styleDangerButton(JButton btn) {
        styleButton(btn, DANGER);
    }

    public static void styleSecondaryButton(JButton btn) {
        applyFlatStyle(btn, new Color(236, 240, 241), TEXT_PRIMARY, new EmptyBorder(10, 18, 10, 18));
    }

    public static void styleButton(JButton btn, Color bg) {
        applyFlatStyle(btn, bg, Color.WHITE, new EmptyBorder(10, 18, 10, 18));
    }

    private static void applyFlatStyle(JButton btn, Color bg, Color fg, EmptyBorder padding) {
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(padding);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, JComponent c) {
                javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill;
                if (!b.isEnabled()) {
                    fill = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 140);
                } else if (b.getModel().isPressed()) {
                    fill = bg.darker();
                } else if (b.getModel().isRollover()) {
                    fill = new Color(
                            Math.min(255, bg.getRed() + 22),
                            Math.min(255, bg.getGreen() + 22),
                            Math.min(255, bg.getBlue() + 22));
                } else {
                    fill = bg;
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                g2.dispose();
                super.paint(g, c);
            }
        });
        // Set AFTER setUI() — BasicButtonUI.installDefaults() runs inside setUI() and
        // resets font/foreground to the LAF default. Setting them last ensures our values win.
        btn.setForeground(fg);
        btn.setFont(bodyFont());
    }

    public static JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(smallFont());
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(bodyFont());
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JTextField createAuthTextField() {
        JTextField field = new JTextField(20);
        field.setFont(bodyFont());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(10, 12, 10, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    public static JPasswordField createAuthPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(bodyFont());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(10, 12, 10, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    public static JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(smallFont());
        btn.setForeground(ACCENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Styles a button for use on dark-background admin panels (coloured variant).
     * Delegates through the shared path so Nimbus renders it correctly on Windows.
     *
     * Design Pattern: used by AdminInventoryPanel and AdminUsersPanel to avoid
     * duplicating private styleButton() methods that bypass cross-platform rendering.
     */
    public static void styleDarkButton(JButton btn, Color bg) {
        applyFlatStyle(btn, bg, Color.WHITE, new EmptyBorder(9, 16, 9, 16));
    }

    /**
     * Styles a button for use on dark-background admin panels (secondary / neutral variant).
     */
    public static void styleDarkSecondaryButton(JButton btn) {
        applyFlatStyle(btn, new Color(52, 73, 94), new Color(236, 240, 241), new EmptyBorder(9, 16, 9, 16));
    }

    /**
     * Styles a sidebar navigation button — slightly darker than the sidebar background so it
     * is visible without blending in, and left-aligned to suit a vertical menu.
     */
    public static void styleNavButton(JButton btn) {
        applyFlatStyle(btn, new Color(44, 62, 80), new Color(236, 240, 241), new EmptyBorder(12, 14, 12, 14));
    }
}
