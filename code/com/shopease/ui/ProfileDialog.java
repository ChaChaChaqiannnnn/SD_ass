package com.shopease.ui;

import com.shopease.model.User;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileDialog extends JDialog {
    public ProfileDialog(JFrame parent, ShopEaseService service, Runnable onProfileUpdated) {
        super(parent, "Profile settings", true);
        setMinimumSize(new Dimension(440, 420));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ShopEaseUIUtils.BG_PAGE);
        setLayout(new BorderLayout());

        User user = service.getCurrentUser();
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ShopEaseUIUtils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 232)),
                new EmptyBorder(24, 28, 24, 28)));

        JLabel title = new JLabel("Profile settings");
        title.setFont(ShopEaseUIUtils.titleFont());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        JLabel subtitle = ShopEaseUIUtils.createMutedLabel("Update your display name, email, or password.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(4));
        JLabel idHint = ShopEaseUIUtils.createMutedLabel("Account ID: " + user.getUserId());
        idHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(idHint);
        card.add(Box.createVerticalStrut(16));

        JTextField nameField = ShopEaseUIUtils.createAuthTextField();
        nameField.setText(user.getName());
        JTextField emailField = ShopEaseUIUtils.createAuthTextField();
        emailField.setText(user.getEmail());
        JPasswordField passField = ShopEaseUIUtils.createAuthPasswordField();
        JPasswordField confirmField = ShopEaseUIUtils.createAuthPasswordField();

        card.add(labeled("Full name", nameField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Email", emailField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("New password (optional)", passField));
        card.add(Box.createVerticalStrut(4));
        JLabel passHint = ShopEaseUIUtils.createMutedLabel("Leave blank to keep your current password.");
        passHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passHint);
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Confirm new password", confirmField));

        JLabel status = ShopEaseUIUtils.createMutedLabel(" ");
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(status);
        card.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton cancel = new JButton("Cancel");
        ShopEaseUIUtils.styleSecondaryButton(cancel);
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton("Save changes");
        ShopEaseUIUtils.stylePrimaryButton(save);
        save.addActionListener(e -> {
            String pass = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());
            if (!pass.isEmpty() && !pass.equals(confirm)) {
                status.setText("Passwords do not match.");
                status.setForeground(ShopEaseUIUtils.DANGER);
                return;
            }
            if (!pass.isEmpty() && pass.length() < 4) {
                status.setText("Password must be at least 4 characters.");
                status.setForeground(ShopEaseUIUtils.DANGER);
                return;
            }
            if (service.updateCustomerProfile(nameField.getText(), emailField.getText(), pass)) {
                if (onProfileUpdated != null) {
                    onProfileUpdated.run();
                }
                JOptionPane.showMessageDialog(this,
                        service.getLastMessage(),
                        "Profile updated",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                status.setText(service.getLastMessage());
                status.setForeground(ShopEaseUIUtils.DANGER);
            }
        });
        actions.add(cancel);
        actions.add(save);
        card.add(actions);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrap.add(card);
        add(wrap, BorderLayout.CENTER);
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = ShopEaseUIUtils.createFieldLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }
}
