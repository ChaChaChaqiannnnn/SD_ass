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

/**
 * Admin — create, edit, delete customers; view orders and wishlist per user.
 * <p>
 * Strategy Pattern — CRUD goes through {@link ShopEaseService} which delegates to
 * CreateCustomerUserStrategy, UpdateCustomerUserStrategy, and DeleteCustomerUserStrategy.
 * Observer Pattern — live refresh via {@link com.shopease.observer.ShopEaseDataChangeRefreshObserver}
 * attached in {@link AdminDashboard} (notifyDataChanged after admin writes).
 */
public class AdminUsersPanel extends JPanel {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    private static final Color FIELD_BG = new Color(236, 240, 241);

    private final ShopEaseService service;
    private final List<User> customers = new ArrayList<>();
    private DefaultListModel<User> listModel;
    private JList<User> userList;
    private JTextField searchField;
    private JLabel userIdLabel;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JPanel ordersPanel;
    private DefaultListModel<String> wishlistModel;
    private JLabel statusLabel;
    private boolean createMode;

    private final JPanel columnsHost;
    private final JPanel customerManagementColumn;
    private final JPanel activityColumn;
    private static final int FIELD_HEIGHT = 30;
    private static final int STACK_BREAKPOINT_PX = 860;
    private static final String NO_SELECTION_MSG = "Select a customer to view details.";

    public AdminUsersPanel(ShopEaseService service) {
        this.service = service;
        setLayout(new BorderLayout(6, 4));
        setOpaque(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(2, 4, 4, 4));
        JButton newBtn = new JButton("New customer");
        compactToolbarButton(newBtn, new Color(52, 152, 219));
        newBtn.addActionListener(e -> startCreateMode());
        JButton saveBtn = new JButton("Save");
        compactToolbarButton(saveBtn, new Color(39, 174, 96));
        saveBtn.addActionListener(e -> saveUser());
        JButton deleteBtn = new JButton("Delete");
        compactToolbarButton(deleteBtn, new Color(192, 57, 43));
        deleteBtn.addActionListener(e -> deleteUser());
        JButton refreshBtn = new JButton("Refresh");
        compactToolbarButton(refreshBtn, new Color(52, 73, 94));
        toolbar.add(newBtn);
        toolbar.add(saveBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        // ── Combined Customer Management panel (~62%) ─────────────────────────
        customerManagementColumn = new JPanel(new BorderLayout(4, 4));
        customerManagementColumn.setOpaque(false);
        customerManagementColumn.setBorder(sectionBorder("Customer Management"));
        customerManagementColumn.add(toolbar, BorderLayout.NORTH);

        searchField = adminField("");
        searchField.setToolTipText("Search by name, email, or ID");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
        });

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setCellRenderer(new CustomerListRenderer());
        userList.setFixedCellHeight(42);
        userList.setBackground(new Color(52, 73, 94));
        userList.setSelectionBackground(new Color(52, 152, 219));
        userList.setSelectionForeground(Color.WHITE);
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedUser();
            }
        });

        JScrollPane listScroll = new JScrollPane(userList);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(127, 140, 141)));
        listScroll.getVerticalScrollBar().setUnitIncrement(16);
        listScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel listArea = new JPanel(new BorderLayout(4, 4));
        listArea.setOpaque(false);
        listArea.setBorder(new EmptyBorder(0, 4, 4, 2));
        listArea.add(searchField, BorderLayout.NORTH);
        listArea.add(listScroll, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(4, 6, 4, 8));

        userIdLabel = new JLabel("—");
        userIdLabel.setFont(ShopEaseUIUtils.bodyFont());
        userIdLabel.setForeground(new Color(189, 195, 199));

        nameField = adminField("Customer full name");
        emailField = adminField("Email address");
        passwordField = adminPassField("Password");
        confirmField = adminPassField("Confirm password");

        GridBagConstraints labelC = new GridBagConstraints();
        labelC.gridx = 0;
        labelC.anchor = GridBagConstraints.WEST;
        labelC.insets = new Insets(0, 0, 2, 8);

        GridBagConstraints valueC = new GridBagConstraints();
        valueC.gridx = 1;
        valueC.weightx = 1.0;
        valueC.fill = GridBagConstraints.HORIZONTAL;
        valueC.insets = new Insets(0, 0, 4, 0);

        int row = 0;
        addFormRow(form, row++, "Account ID", userIdLabel, labelC, valueC);
        addFormRow(form, row++, "Full name", nameField, labelC, valueC);
        addFormRow(form, row++, "Email", emailField, labelC, valueC);
        addFormRow(form, row++, "Password", passwordField, labelC, valueC);

        GridBagConstraints hintC = new GridBagConstraints();
        hintC.gridx = 1;
        hintC.gridy = row++;
        hintC.weightx = 1.0;
        hintC.fill = GridBagConstraints.HORIZONTAL;
        hintC.insets = new Insets(0, 0, 4, 0);
        JLabel passHint = new JLabel("Leave blank to keep current password.");
        passHint.setFont(ShopEaseUIUtils.smallFont());
        passHint.setForeground(new Color(149, 165, 166));
        form.add(passHint, hintC);

        addFormRow(form, row, "Confirm password", confirmField, labelC, valueC);

        JPanel detailsArea = new JPanel(new BorderLayout());
        detailsArea.setOpaque(false);
        detailsArea.setBorder(new EmptyBorder(0, 2, 4, 4));
        detailsArea.add(form, BorderLayout.NORTH);

        JSplitPane managementSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        managementSplit.setOpaque(false);
        managementSplit.setBorder(null);
        managementSplit.setDividerSize(4);
        managementSplit.setResizeWeight(0.40);
        managementSplit.setLeftComponent(listArea);
        managementSplit.setRightComponent(detailsArea);
        customerManagementColumn.add(managementSplit, BorderLayout.CENTER);

        // ── Orders & Wishlist panel (~38%) ────────────────────────────────────
        activityColumn = new JPanel(new BorderLayout(4, 4));
        activityColumn.setOpaque(false);
        activityColumn.setBorder(sectionBorder("Orders & Wishlist"));

        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(new Color(52, 73, 94));
        ordersPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane ordersScroll = new JScrollPane(ordersPanel);
        ordersScroll.setBorder(null);
        ordersScroll.getVerticalScrollBar().setUnitIncrement(16);
        ordersScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        wishlistModel = new DefaultListModel<>();
        JList<String> wishlistList = new JList<>(wishlistModel);
        wishlistList.setFont(ShopEaseUIUtils.bodyFont());
        wishlistList.setBackground(new Color(52, 73, 94));
        wishlistList.setForeground(new Color(236, 240, 241));
        wishlistList.setFixedCellHeight(28);
        wishlistList.setBorder(new EmptyBorder(4, 6, 4, 6));

        JScrollPane wishScroll = new JScrollPane(wishlistList);
        wishScroll.setBorder(null);
        wishScroll.getVerticalScrollBar().setUnitIncrement(16);
        wishScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JTabbedPane activityTabs = new JTabbedPane();
        activityTabs.setFont(ShopEaseUIUtils.smallFont());
        activityTabs.setBorder(new EmptyBorder(0, 4, 2, 4));
        activityTabs.addTab("Order history", ordersScroll);
        activityTabs.addTab("Wishlist", wishScroll);
        activityColumn.add(activityTabs, BorderLayout.CENTER);

        columnsHost = new JPanel();
        columnsHost.setOpaque(false);
        columnsHost.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                relayoutColumns();
            }
        });
        add(columnsHost, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ShopEaseUIUtils.smallFont());
        statusLabel.setForeground(new Color(189, 195, 199));
        statusLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);

        relayoutColumns();
        refreshAll();
    }

    /** Wide: Customer Management (~62%) | Orders & Wishlist (~38%). Narrow: stack vertically. */
    private void relayoutColumns() {
        columnsHost.removeAll();
        int width = columnsHost.getWidth();
        if (width <= 0) {
            width = getWidth() > 0 ? getWidth() : STACK_BREAKPOINT_PX;
        }

        if (width < STACK_BREAKPOINT_PX) {
            columnsHost.setLayout(new BoxLayout(columnsHost, BoxLayout.Y_AXIS));
            prepareStackedColumn(customerManagementColumn, 380);
            prepareStackedColumn(activityColumn, 260);
            columnsHost.add(customerManagementColumn);
            columnsHost.add(Box.createVerticalStrut(6));
            columnsHost.add(activityColumn);
        } else {
            columnsHost.setLayout(new GridBagLayout());
            prepareSideBySideColumn(customerManagementColumn);
            prepareSideBySideColumn(activityColumn);
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1.0;

            c.gridx = 0;
            c.weightx = 0.62;
            c.insets = new Insets(0, 0, 0, 4);
            columnsHost.add(customerManagementColumn, c);

            c.gridx = 1;
            c.weightx = 0.38;
            c.insets = new Insets(0, 0, 0, 0);
            columnsHost.add(activityColumn, c);
        }
        columnsHost.revalidate();
        columnsHost.repaint();
    }

    private static void prepareStackedColumn(JPanel panel, int height) {
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        panel.setPreferredSize(new Dimension(10, height));
    }

    private static void prepareSideBySideColumn(JPanel panel) {
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setPreferredSize(null);
    }

    public void refreshAll() {
        customers.clear();
        customers.addAll(service.getAllUsersForAdmin());
        filterList();
        if (userList.getSelectedIndex() >= 0) {
            loadSelectedUser();
        } else if (!createMode) {
            clearForm();
        }
    }

    private void filterList() {
        String q = searchField.getText().trim().toLowerCase();
        listModel.clear();
        for (User u : customers) {
            if (q.isEmpty()
                    || u.getName().toLowerCase().contains(q)
                    || u.getEmail().toLowerCase().contains(q)
                    || u.getUserId().toLowerCase().contains(q)) {
                listModel.addElement(u);
            }
        }
        if (listModel.isEmpty()) {
            showStatus(q.isEmpty() ? "No customers yet — click New customer." : "No customers match your search.", false);
        }
    }

    private User getSelectedCustomer() {
        return userList.getSelectedValue();
    }

    private void loadSelectedUser() {
        createMode = false;
        User u = getSelectedCustomer();
        if (u == null) {
            if (userList.getSelectedIndex() < 0 && !createMode) {
                clearForm();
            }
            return;
        }
        userIdLabel.setText(u.getUserId());
        nameField.setText(u.getName());
        emailField.setText(u.getEmail());
        passwordField.setText("");
        confirmField.setText("");
        loadOrders(u.getUserId());
        loadWishlist(u.getUserId());
        showStatus("Viewing " + u.getName(), false);
    }

    private void loadOrders(String userId) {
        ordersPanel.removeAll();
        List<Order> orders = service.getOrdersForUserAsAdmin(userId);
        if (orders.isEmpty()) {
            ordersPanel.add(mutedLine("No orders for this customer yet."));
        } else {
            for (int i = 0; i < orders.size(); i++) {
                ordersPanel.add(buildOrderBlock(orders.get(i)));
                if (i < orders.size() - 1) {
                    ordersPanel.add(Box.createVerticalStrut(10));
                }
            }
        }
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private JPanel buildOrderBlock(Order order) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel dateLine = new JLabel(DATE_FMT.format(order.getOrderDate()));
        dateLine.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        dateLine.setForeground(new Color(236, 240, 241));
        dateLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summary = new JLabel(String.format("RM %.2f  ·  %s",
                order.getTotalAmount(), order.getStatus()));
        summary.setFont(ShopEaseUIUtils.smallFont());
        summary.setForeground(new Color(189, 195, 199));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(dateLine);
        block.add(Box.createVerticalStrut(4));
        block.add(summary);
        block.add(Box.createVerticalStrut(6));

        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                JLabel line = new JLabel("  •  " + item.getProduct().getName()
                        + "  ×  " + item.getQuantity());
                line.setFont(ShopEaseUIUtils.smallFont());
                line.setForeground(new Color(236, 240, 241));
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                block.add(line);
            }
        }
        return block;
    }

    private void loadWishlist(String userId) {
        wishlistModel.clear();
        List<Product> items = service.getWishlistForUserAsAdmin(userId);
        if (items.isEmpty()) {
            wishlistModel.addElement("Wishlist is empty.");
            return;
        }
        for (Product p : items) {
            wishlistModel.addElement(p.getName() + "  ·  RM " + String.format("%.2f", p.getPrice())
                    + "  ·  " + p.getStockQuantity() + " in stock");
        }
    }

    private void startCreateMode() {
        createMode = true;
        userList.clearSelection();
        userIdLabel.setText("(assigned when you save)");
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        ordersPanel.removeAll();
        ordersPanel.add(mutedLine("Save a new customer to view their orders."));
        ordersPanel.revalidate();
        wishlistModel.clear();
        wishlistModel.addElement("New customer — no wishlist yet.");
        showStatus("Fill in the form below, then click Save.", false);
        nameField.requestFocus();
    }

    private void saveUser() {
        String name    = nameField.getText();
        String email   = emailField.getText();
        String pass    = new String(passwordField.getPassword());
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
            final String fn = name, fe = email, fp = pass;
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return service.createCustomerAsAdmin(fn, fe, fp);
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            showStatus(service.getLastMessage(), false);
                            createMode = false;
                            refreshAll();
                        } else {
                            showStatus(service.getLastMessage(), true);
                        }
                    } catch (Exception ex) {
                        showStatus("Create error. Please try again.", true);
                    }
                }
            }.execute();
            return;
        }
        User u = getSelectedCustomer();
        if (u == null) {
            showStatus("Select a customer or click New customer.", true);
            return;
        }
        final String uid = u.getUserId();
        final String fn2 = name, fe2 = email, fp2 = pass;
        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return service.updateCustomerAsAdmin(uid, fn2, fe2, fp2);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showStatus(service.getLastMessage(), false);
                        refreshAll();
                        selectCustomerById(uid);
                    } else {
                        showStatus(service.getLastMessage(), true);
                    }
                } catch (Exception ex) {
                    showStatus("Update error. Please try again.", true);
                }
            }
        }.execute();
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
        final String uid = u.getUserId();
        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return service.deleteUserAsAdmin(uid);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showStatus(service.getLastMessage(), false);
                        clearForm();
                        refreshAll();
                    } else {
                        showStatus(service.getLastMessage(), true);
                    }
                } catch (Exception ex) {
                    showStatus("Delete error. Please try again.", true);
                }
            }
        }.execute();
    }

    private void selectCustomerById(String userId) {
        for (int i = 0; i < listModel.size(); i++) {
            User u = listModel.getElementAt(i);
            if (u.getUserId().equals(userId)) {
                userList.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearForm() {
        userIdLabel.setText("—");
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        ordersPanel.removeAll();
        ordersPanel.add(mutedLine(NO_SELECTION_MSG));
        ordersPanel.revalidate();
        ordersPanel.repaint();
        wishlistModel.clear();
        wishlistModel.addElement(NO_SELECTION_MSG);
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setForeground(error ? new Color(231, 76, 60) : new Color(46, 204, 113));
    }

    private static TitledBorder sectionBorder(String title) {
        return new TitledBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.smallFont(), new Color(236, 240, 241));
    }

    private static void addFormRow(JPanel form, int row, String label, JComponent value,
                                   GridBagConstraints labelC, GridBagConstraints valueC) {
        JLabel l = new JLabel(label);
        l.setFont(ShopEaseUIUtils.smallFont());
        l.setForeground(new Color(189, 195, 199));
        labelC.gridy = row;
        valueC.gridy = row;
        form.add(l, labelC);
        form.add(value, valueC);
    }

    private static void compactToolbarButton(JButton btn, Color bg) {
        if (new Color(52, 73, 94).equals(bg)) {
            ShopEaseUIUtils.styleDarkSecondaryButton(btn);
        } else {
            ShopEaseUIUtils.styleDarkButton(btn, bg);
        }
        btn.setFont(ShopEaseUIUtils.smallFont());
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
    }

    private static JLabel mutedLine(String text) {
        JLabel line = new JLabel(text);
        line.setFont(ShopEaseUIUtils.smallFont());
        line.setForeground(new Color(189, 195, 199));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private static JTextField adminField(String tooltip) {
        JTextField f = new JTextField();
        f.setFont(ShopEaseUIUtils.bodyFont());
        f.setBackground(FIELD_BG);
        f.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                new EmptyBorder(4, 8, 4, 8)));
        f.setPreferredSize(new Dimension(200, FIELD_HEIGHT));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        if (!tooltip.isEmpty()) {
            f.setToolTipText(tooltip);
        }
        return f;
    }

    private static JPasswordField adminPassField(String tooltip) {
        JPasswordField f = new JPasswordField();
        f.setFont(ShopEaseUIUtils.bodyFont());
        f.setBackground(FIELD_BG);
        f.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                new EmptyBorder(4, 8, 4, 8)));
        f.setPreferredSize(new Dimension(200, FIELD_HEIGHT));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        f.setToolTipText(tooltip);
        return f;
    }

    private static String ellipsisText(String text, FontMetrics fm, int maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return text == null ? "" : text;
        }
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        for (int i = text.length() - 1; i > 0; i--) {
            String candidate = text.substring(0, i) + suffix;
            if (fm.stringWidth(candidate) <= maxWidth) {
                return candidate;
            }
        }
        return suffix;
    }

    /** Two-line list cell — name on top, email below with ellipsis for long text. */
    private static class CustomerListRenderer extends JPanel implements ListCellRenderer<User> {
        private final JLabel nameLabel = new JLabel();
        private final JLabel emailLabel = new JLabel();

        CustomerListRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(4, 8, 4, 8));
            setOpaque(true);
            JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
            text.setOpaque(false);
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            emailLabel.setFont(ShopEaseUIUtils.smallFont());
            text.add(nameLabel);
            text.add(emailLabel);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User user,
                                                      int index, boolean selected, boolean focus) {
            if (user == null) {
                nameLabel.setText("");
                emailLabel.setText("");
                nameLabel.setToolTipText(null);
                emailLabel.setToolTipText(null);
                return this;
            }
            int maxWidth = Math.max(list.getWidth() - 24, 80);
            FontMetrics nameFm = nameLabel.getFontMetrics(nameLabel.getFont());
            FontMetrics emailFm = emailLabel.getFontMetrics(emailLabel.getFont());
            String name = ellipsisText(user.getName(), nameFm, maxWidth);
            String email = ellipsisText(user.getEmail(), emailFm, maxWidth);
            nameLabel.setText(name);
            emailLabel.setText(email);
            nameLabel.setToolTipText(user.getName());
            emailLabel.setToolTipText(user.getEmail());
            if (selected) {
                setBackground(new Color(52, 152, 219));
                nameLabel.setForeground(Color.WHITE);
                emailLabel.setForeground(new Color(220, 230, 240));
            } else {
                setBackground(new Color(52, 73, 94));
                nameLabel.setForeground(new Color(236, 240, 241));
                emailLabel.setForeground(new Color(149, 165, 166));
            }
            return this;
        }
    }
}
