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
        btn.setBackground(new Color(236, 240, 241));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Styles a button for use on dark-background admin panels (secondary / neutral variant).
     */
    public static void styleDarkSecondaryButton(JButton btn) {
        styleDarkButton(btn, new Color(52, 73, 94));
        btn.setForeground(new Color(236, 240, 241));
    }
}
