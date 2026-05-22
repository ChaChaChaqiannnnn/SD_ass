package com.shopease.ui;

import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.model.Product;
import com.shopease.model.User;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Admin sidebar section: customer CRUD, orders, wishlist. */
public class AdminUsersPanel extends JPanel {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    private final ShopEaseService service;
    private final List<User> customers = new ArrayList<>();
    private DefaultListModel<String> listModel;
    private JList<String> userList;
    private JTextField searchField;
    private JTextField userIdField;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JTextArea ordersArea;
    private DefaultListModel<String> wishlistModel;
    private JLabel statusLabel;
    private boolean createMode;

    public AdminUsersPanel(ShopEaseService service) {
        this.service = service;
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Manage users");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setForeground(new Color(236, 240, 241));
        JLabel hint = new JLabel("Create, edit, or delete customers. View orders and wishlist per user.");
        hint.setFont(ShopEaseUIUtils.smallFont());
        hint.setForeground(new Color(189, 195, 199));
        header.add(title, BorderLayout.NORTH);
        header.add(hint, BorderLayout.SOUTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(8, 0, 0, 0));
        JButton newBtn = new JButton("New customer");
        styleAccent(newBtn, new Color(52, 152, 219));
        newBtn.addActionListener(e -> startCreateMode());
        JButton saveBtn = new JButton("Save");
        styleAccent(saveBtn, new Color(39, 174, 96));
        saveBtn.addActionListener(e -> saveUser());
        JButton deleteBtn = new JButton("Delete");
        styleAccent(deleteBtn, new Color(192, 57, 43));
        deleteBtn.addActionListener(e -> deleteUser());
        JButton refreshBtn = new JButton("Refresh");
        styleSecondary(refreshBtn);
        refreshBtn.addActionListener(e -> refreshAll());
        toolbar.add(newBtn);
        toolbar.add(saveBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(header);
        north.add(toolbar);
        add(north, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.32);
        split.setOpaque(false);
        split.setBorder(null);

        JPanel listPanel = new JPanel(new BorderLayout(8, 8));
        listPanel.setOpaque(false);
        listPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                "Customers", TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.bodyFont(), new Color(236, 240, 241)));

        searchField = new JTextField();
        searchField.setFont(ShopEaseUIUtils.bodyFont());
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                new EmptyBorder(8, 10, 8, 10)));
        searchField.setToolTipText("Search by name, email, or ID");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
        });
        listPanel.add(searchField, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setFont(ShopEaseUIUtils.bodyFont());
        userList.setBackground(new Color(52, 73, 94));
        userList.setForeground(Color.WHITE);
        userList.setSelectionBackground(new Color(52, 152, 219));
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedUser();
            }
        });
        listPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        split.setLeftComponent(listPanel);

        JPanel detailPanel = new JPanel(new BorderLayout(8, 8));
        detailPanel.setOpaque(false);
        detailPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                "Customer details", TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.bodyFont(), new Color(236, 240, 241)));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(8, 12, 8, 12));

        userIdField = field("User ID (auto on create)");
        userIdField.setEditable(false);
        nameField = field("Full name");
        emailField = field("Email");
        passwordField = passField("Password");
        confirmField = passField("Confirm password");
        JLabel passHint = new JLabel("Leave password blank when editing to keep current.");
        passHint.setFont(ShopEaseUIUtils.smallFont());
        passHint.setForeground(new Color(189, 195, 199));
        passHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(wrap(userIdField, "User ID"));
        form.add(Box.createVerticalStrut(8));
        form.add(wrap(nameField, "Full name"));
        form.add(Box.createVerticalStrut(8));
        form.add(wrap(emailField, "Email"));
        form.add(Box.createVerticalStrut(8));
        form.add(wrap(passwordField, "Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(passHint);
        form.add(Box.createVerticalStrut(8));
        form.add(wrap(confirmField, "Confirm password"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ShopEaseUIUtils.bodyFont());

        ordersArea = new JTextArea();
        ordersArea.setEditable(false);
        ordersArea.setFont(ShopEaseUIUtils.bodyFont());
        ordersArea.setBackground(new Color(52, 73, 94));
        ordersArea.setForeground(new Color(236, 240, 241));
        ordersArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabs.addTab("Order history", new JScrollPane(ordersArea));

        wishlistModel = new DefaultListModel<>();
        JList<String> wishlistList = new JList<>(wishlistModel);
        wishlistList.setFont(ShopEaseUIUtils.bodyFont());
        wishlistList.setBackground(new Color(52, 73, 94));
        wishlistList.setForeground(Color.WHITE);
        tabs.addTab("Wishlist", new JScrollPane(wishlistList));

        JPanel rightSplit = new JPanel(new BorderLayout(8, 0));
        rightSplit.setOpaque(false);
        rightSplit.add(form, BorderLayout.NORTH);
        rightSplit.add(tabs, BorderLayout.CENTER);
        detailPanel.add(rightSplit, BorderLayout.CENTER);
        split.setRightComponent(detailPanel);

        add(split, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ShopEaseUIUtils.smallFont());
        statusLabel.setForeground(new Color(189, 195, 199));
        statusLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);

        refreshAll();
    }

    public void refreshAll() {
        customers.clear();
        customers.addAll(service.getAllUsersForAdmin());
        filterList();
        if (userList.getSelectedIndex() >= 0) {
            loadSelectedUser();
        }
    }

    private void filterList() {
        String q = searchField.getText().trim().toLowerCase();
        listModel.clear();
        int shown = 0;
        for (User u : customers) {
            String line = u.getName() + " · " + u.getEmail();
            if (q.isEmpty()
                    || u.getName().toLowerCase().contains(q)
                    || u.getEmail().toLowerCase().contains(q)
                    || u.getUserId().toLowerCase().contains(q)) {
                listModel.addElement(line);
                shown++;
            }
        }
        if (shown == 0) {
            listModel.addElement("(no customers match)");
        }
    }

    private User getSelectedCustomer() {
        int idx = userList.getSelectedIndex();
        if (idx < 0) {
            return null;
        }
        String selected = listModel.getElementAt(idx);
        if (selected.startsWith("(")) {
            return null;
        }
        for (User u : customers) {
            String line = u.getName() + " · " + u.getEmail();
            if (line.equals(selected)) {
                return u;
            }
        }
        return null;
    }

    private void loadSelectedUser() {
        createMode = false;
        User u = getSelectedCustomer();
        if (u == null) {
            clearForm();
            return;
        }
        userIdField.setText(u.getUserId());
        nameField.setText(u.getName());
        emailField.setText(u.getEmail());
        passwordField.setText("");
        confirmField.setText("");
        loadOrders(u.getUserId());
        loadWishlist(u.getUserId());
        showStatus("Viewing " + u.getName(), false);
    }

    private void loadOrders(String userId) {
        List<Order> orders = service.getOrdersForUserAsAdmin(userId);
        if (orders.isEmpty()) {
            ordersArea.setText("No orders for this customer.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Order o : orders) {
            sb.append("Order ").append(o.getOrderId())
              .append("\n  ").append(DATE_FMT.format(o.getOrderDate()))
              .append("  ·  RM ").append(String.format("%.2f", o.getTotalAmount()))
              .append("  ·  ").append(o.getStatus()).append("\n");
            if (o.getItems() != null) {
                for (CartItem item : o.getItems()) {
                    sb.append("    • ").append(item.getProduct().getName())
                      .append(" × ").append(item.getQuantity()).append("\n");
                }
            }
            sb.append("\n");
        }
        ordersArea.setText(sb.toString());
        ordersArea.setCaretPosition(0);
    }

    private void loadWishlist(String userId) {
        wishlistModel.clear();
        List<Product> items = service.getWishlistForUserAsAdmin(userId);
        if (items.isEmpty()) {
            wishlistModel.addElement("(wishlist empty)");
            return;
        }
        for (Product p : items) {
            wishlistModel.addElement(p.getName() + "  ·  RM " + String.format("%.2f", p.getPrice())
                    + "  ·  stock: " + p.getStockQuantity());
        }
    }

    private void startCreateMode() {
        createMode = true;
        userList.clearSelection();
        userIdField.setText("(assigned on save)");
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        ordersArea.setText("Save a new customer to view their orders.");
        wishlistModel.clear();
        wishlistModel.addElement("(new customer)");
        showStatus("Creating new customer — fill the form and click Save.", false);
        nameField.requestFocus();
    }

    private void saveUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (!pass.equals(confirm)) {
            showStatus("Passwords do not match.", true);
            return;
        }
        if (createMode) {
            if (pass.isEmpty()) {
                showStatus("Password is required for new customers.", true);
                return;
            }
            if (service.createCustomerAsAdmin(name, email, pass)) {
                showStatus(service.getLastMessage(), false);
                refreshAll();
                createMode = false;
            } else {
                showStatus(service.getLastMessage(), true);
            }
            return;
        }
        User u = getSelectedCustomer();
        if (u == null) {
            showStatus("Select a customer or click New customer.", true);
            return;
        }
        if (service.updateCustomerAsAdmin(u.getUserId(), name, email, pass)) {
            showStatus(service.getLastMessage(), false);
            refreshAll();
            selectCustomerById(u.getUserId());
        } else {
            showStatus(service.getLastMessage(), true);
        }
    }

    private void deleteUser() {
        User u = getSelectedCustomer();
        if (u == null) {
            showStatus("Select a customer to delete.", true);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Delete <b>" + u.getName() + "</b> (" + u.getEmail() + ")?<br>"
                        + "This removes their orders and wishlist.</html>",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (service.deleteUserAsAdmin(u.getUserId())) {
            showStatus(service.getLastMessage(), false);
            clearForm();
            refreshAll();
        } else {
            showStatus(service.getLastMessage(), true);
        }
    }

    private void selectCustomerById(String userId) {
        for (int i = 0; i < listModel.size(); i++) {
            String el = listModel.getElementAt(i);
            for (User u : customers) {
                if (u.getUserId().equals(userId) && (u.getName() + " · " + u.getEmail()).equals(el)) {
                    userList.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void clearForm() {
        userIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        ordersArea.setText("");
        wishlistModel.clear();
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setForeground(error ? new Color(231, 76, 60) : new Color(46, 204, 113));
    }

    private static JTextField field(String tooltip) {
        JTextField f = ShopEaseUIUtils.createAuthTextField();
        f.setToolTipText(tooltip);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height));
        return f;
    }

    private static JPasswordField passField(String tooltip) {
        JPasswordField f = ShopEaseUIUtils.createAuthPasswordField();
        f.setToolTipText(tooltip);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height));
        return f;
    }

    private static JPanel wrap(JComponent field, String label) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = ShopEaseUIUtils.createFieldLabel(label);
        l.setForeground(new Color(236, 240, 241));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }

    private void styleAccent(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(ShopEaseUIUtils.bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondary(JButton btn) {
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(new Color(236, 240, 241));
        btn.setFont(ShopEaseUIUtils.bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }
}
