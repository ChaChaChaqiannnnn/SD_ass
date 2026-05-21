package com.shopease.ui;

import com.shopease.model.AdminInventoryLog;
import com.shopease.model.Product;
import com.shopease.service.DataChangeListener;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminDashboard extends JPanel {
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");

    private final ShopEaseService service;
    private final Runnable onLogout;
    private DefaultListModel<Product> listModel;
    private JList<Product> productList;
    private JTextArea historyArea;
    private JLabel statusLabel;
    private JButton undoBtn;
    private JSpinner restockSpinner;
    private JSpinner reduceSpinner;
    private JTextField remarksField;
    private final DataChangeListener liveSyncListener;

    public AdminDashboard(ShopEaseService service, Runnable onLogout) {
        this.service = service;
        this.liveSyncListener = this::refreshAll;
        service.addDataChangeListener(liveSyncListener);
        this.onLogout = () -> {
            service.removeDataChangeListener(liveSyncListener);
            onLogout.run();
        };
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setBackground(new Color(44, 62, 80));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Admin Control Panel");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(236, 240, 241));
        JLabel hint = new JLabel("Restock or reduce stock. Reductions require remarks. Undo reverses your last change.");
        hint.setFont(ShopEaseUIUtils.smallFont());
        hint.setForeground(new Color(189, 195, 199));
        header.add(welcomeLabel);
        header.add(hint);
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setOpaque(false);
        split.setBorder(null);

        JPanel productsPanel = new JPanel(new BorderLayout(8, 8));
        productsPanel.setOpaque(false);
        productsPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                "Inventory", TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.bodyFont(), new Color(236, 240, 241)));

        listModel = new DefaultListModel<>();
        productList = new JList<>(listModel);
        productList.setCellRenderer(new AdminProductCellRenderer());
        productList.setFont(ShopEaseUIUtils.bodyFont());
        productList.setBackground(new Color(52, 73, 94));
        productList.setForeground(Color.WHITE);
        productList.setSelectionBackground(new Color(52, 152, 219));

        JScrollPane productScroll = new JScrollPane(productList);
        productScroll.setBorder(BorderFactory.createEmptyBorder());
        productsPanel.add(productScroll, BorderLayout.CENTER);
        split.setLeftComponent(productsPanel);

        JPanel historyPanel = new JPanel(new BorderLayout(8, 8));
        historyPanel.setOpaque(false);
        historyPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                "Inventory history (your actions)", TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.bodyFont(), new Color(236, 240, 241)));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyArea.setBackground(new Color(52, 73, 94));
        historyArea.setForeground(new Color(236, 240, 241));
        historyArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JButton refreshHistoryBtn = new JButton("Refresh");
        styleSecondaryDark(refreshHistoryBtn);
        refreshHistoryBtn.addActionListener(e -> refreshHistory());
        JPanel historyTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        historyTop.setOpaque(false);
        historyTop.add(refreshHistoryBtn);
        historyPanel.add(historyTop, BorderLayout.NORTH);
        split.setRightComponent(historyPanel);

        add(split, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel restockRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        restockRow.setOpaque(false);
        restockRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel amountLabel = new JLabel("Add quantity:");
        amountLabel.setForeground(new Color(236, 240, 241));
        amountLabel.setFont(ShopEaseUIUtils.bodyFont());
        restockSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 9999, 1));
        restockSpinner.setFont(ShopEaseUIUtils.bodyFont());

        JButton restockBtn = new JButton("Restock selected");
        styleButton(restockBtn, new Color(39, 174, 96));
        restockBtn.addActionListener(e -> doRestock((int) restockSpinner.getValue()));

        JButton quickTenBtn = new JButton("Quick +10");
        styleSecondaryDark(quickTenBtn);
        quickTenBtn.addActionListener(e -> doRestock(10));

        undoBtn = new JButton("Undo last change");
        styleButton(undoBtn, new Color(241, 196, 15));
        undoBtn.setForeground(new Color(44, 62, 80));
        undoBtn.addActionListener(e -> doUndo());

        restockRow.add(amountLabel);
        restockRow.add(restockSpinner);
        restockRow.add(restockBtn);
        restockRow.add(quickTenBtn);
        restockRow.add(undoBtn);
        controls.add(restockRow);

        JPanel reduceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        reduceRow.setOpaque(false);
        reduceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel reduceLabel = new JLabel("Reduce by:");
        reduceLabel.setForeground(new Color(236, 240, 241));
        reduceLabel.setFont(ShopEaseUIUtils.bodyFont());
        reduceSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        reduceSpinner.setFont(ShopEaseUIUtils.bodyFont());

        JLabel remarksLabel = new JLabel("Remarks (required):");
        remarksLabel.setForeground(new Color(236, 240, 241));
        remarksLabel.setFont(ShopEaseUIUtils.bodyFont());
        remarksField = new JTextField(22);
        remarksField.setFont(ShopEaseUIUtils.bodyFont());
        remarksField.setToolTipText("e.g. Damaged units, inventory audit correction");

        JButton reduceBtn = new JButton("Reduce stock");
        styleButton(reduceBtn, new Color(192, 57, 43));
        reduceBtn.addActionListener(e -> doReduce());

        reduceRow.add(reduceLabel);
        reduceRow.add(reduceSpinner);
        reduceRow.add(remarksLabel);
        reduceRow.add(remarksField);
        reduceRow.add(reduceBtn);
        controls.add(reduceRow);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ShopEaseUIUtils.smallFont());
        statusLabel.setForeground(new Color(189, 195, 199));
        statusLabel.setBorder(new EmptyBorder(4, 4, 8, 4));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(statusLabel);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> {
            service.logout();
            onLogout.run();
        });
        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutWrap.setOpaque(false);
        logoutWrap.add(logoutBtn);
        bottomRow.add(logoutWrap, BorderLayout.EAST);
        controls.add(bottomRow);

        add(controls, BorderLayout.SOUTH);

        refreshAll();
    }

    private void refreshAll() {
        refreshList();
        refreshHistory();
        updateUndoButton();
    }

    private Product getSelectedProduct() {
        return productList.getSelectedValue();
    }

    private void doRestock(int amount) {
        Product p = getSelectedProduct();
        if (p == null) {
            showStatus("Select a product from the list first.", true);
            return;
        }
        if (service.restockProduct(p.getProductId(), amount)) {
            showStatus(service.getLastMessage(), false);
        } else {
            showStatus(service.getLastMessage(), true);
        }
    }

    private void doReduce() {
        Product p = getSelectedProduct();
        if (p == null) {
            showStatus("Select a product from the list first.", true);
            return;
        }
        String remarks = remarksField.getText().trim();
        if (remarks.isEmpty()) {
            showStatus("Remarks are required before reducing stock.", true);
            remarksField.requestFocus();
            return;
        }
        int amount = (int) reduceSpinner.getValue();
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Reduce <b>" + amount + "</b> from <b>" + p.getName() + "</b>?<br><br>"
                        + "Reason: " + remarks + "</html>",
                "Confirm stock reduction",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (service.reduceStockProduct(p.getProductId(), amount, remarks)) {
            showStatus(service.getLastMessage(), false);
            remarksField.setText("");
        } else {
            showStatus(service.getLastMessage(), true);
        }
    }

    private void doUndo() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Revert your last stock change (restock or reduce) for this session?",
                "Confirm undo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (service.undoLastRestock()) {
            showStatus(service.getLastMessage(), false);
        } else {
            showStatus(service.getLastMessage(), true);
        }
    }

    private void refreshList() {
        listModel.clear();
        service.getAllProducts().forEach(listModel::addElement);
    }

    private void refreshHistory() {
        List<AdminInventoryLog> logs = service.getAdminActivityHistory();
        if (logs.isEmpty()) {
            historyArea.setText("No inventory activity yet.\nRestocks, reductions, and undos will appear here.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (AdminInventoryLog log : logs) {
            sb.append(TIME_FMT.format(log.getTimestamp()))
              .append("  ")
              .append(log.getSummaryLine())
              .append("\n");
        }
        historyArea.setText(sb.toString());
        historyArea.setCaretPosition(0);
    }

    private void updateUndoButton() {
        undoBtn.setEnabled(service.canUndoLastRestock());
        if (!service.canUndoLastRestock()) {
            undoBtn.setToolTipText("No stock change to undo in this session");
        } else {
            undoBtn.setToolTipText("Reverts only your most recent restock or reduction");
        }
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(231, 76, 60) : new Color(46, 204, 113));
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(ShopEaseUIUtils.bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryDark(JButton btn) {
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(new Color(236, 240, 241));
        btn.setFont(ShopEaseUIUtils.bodyFont());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    private static class AdminProductCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product p) {
                String status = p.getStockQuantity() < 5 ? "LOW STOCK" : "OK";
                setText(p.getName() + "  ·  Stock: " + p.getStockQuantity() + "  ·  " + status);
                setBorder(new EmptyBorder(10, 10, 10, 10));
                if (p.getStockQuantity() < 5 && !isSelected) {
                    setForeground(new Color(231, 76, 60));
                } else if (!isSelected) {
                    setForeground(Color.WHITE);
                }
            }
            return this;
        }
    }
}
