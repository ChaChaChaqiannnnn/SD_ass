package com.shopease.ui;

import com.shopease.model.AdminInventoryLog;
import com.shopease.model.InventoryStockStatus;
import com.shopease.model.Product;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/** Admin sidebar section: inventory restock, reduce, history. */
public class AdminInventoryPanel extends JPanel {
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");

    private final ShopEaseService service;
    private DefaultListModel<Product> listModel;
    private JList<Product> productList;
    private JTextArea historyArea;
    private JLabel statusLabel;
    private JButton undoBtn;
    private JSpinner restockSpinner;
    private JSpinner reduceSpinner;
    private JTextField remarksField;

    public AdminInventoryPanel(ShopEaseService service) {
        this.service = service;
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel title = new JLabel("Inventory management");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setForeground(new Color(236, 240, 241));
        JLabel hint = new JLabel("Restock or reduce stock. Reductions require remarks. Undo reverses your last change.");
        hint.setFont(ShopEaseUIUtils.smallFont());
        hint.setForeground(new Color(189, 195, 199));
        header.add(title);
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
                "Products", TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.bodyFont(), new Color(236, 240, 241)));

        listModel = new DefaultListModel<>();
        productList = new JList<>(listModel);
        productList.setCellRenderer(new AdminProductCellRenderer());
        productList.setFont(ShopEaseUIUtils.bodyFont());
        productList.setBackground(new Color(52, 73, 94));
        productList.setForeground(Color.WHITE);
        productList.setSelectionBackground(new Color(52, 152, 219));
        productsPanel.add(new JScrollPane(productList), BorderLayout.CENTER);
        split.setLeftComponent(productsPanel);

        JPanel historyPanel = new JPanel(new BorderLayout(8, 8));
        historyPanel.setOpaque(false);
        historyPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                "Your activity log", TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.bodyFont(), new Color(236, 240, 241)));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyArea.setBackground(new Color(52, 73, 94));
        historyArea.setForeground(new Color(236, 240, 241));
        historyArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JButton refreshHistoryBtn = new JButton("Refresh log");
        ShopEaseUIUtils.styleDarkSecondaryButton(refreshHistoryBtn);
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
        ShopEaseUIUtils.styleDarkButton(restockBtn, new Color(39, 174, 96));
        restockBtn.addActionListener(e -> doRestock((int) restockSpinner.getValue()));
        JButton quickTenBtn = new JButton("Quick +10");
        ShopEaseUIUtils.styleDarkSecondaryButton(quickTenBtn);
        quickTenBtn.addActionListener(e -> doRestock(10));
        undoBtn = new JButton("Undo last change");
        ShopEaseUIUtils.styleDarkButton(undoBtn, new Color(180, 140, 0));
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
        remarksField = new JTextField(15);
        remarksField.setFont(ShopEaseUIUtils.bodyFont());
        JButton reduceBtn = new JButton("Reduce stock");
        ShopEaseUIUtils.styleDarkButton(reduceBtn, new Color(192, 57, 43));
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
        statusLabel.setBorder(new EmptyBorder(4, 4, 0, 4));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(statusLabel);
        add(controls, BorderLayout.SOUTH);

        refreshAll();
    }

    public void refreshAll() {
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
        // SwingWorker — moves SQLite stock update off the EDT to prevent Windows freeze
        // Strategy pattern — ShopEaseService delegates to AdminUserActionStrategy internally
        final String productId = p.getProductId();
        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return service.restockProduct(productId, amount);
            }
            @Override
            protected void done() {
                try {
                    showStatus(service.getLastMessage(), !get());
                } catch (Exception ex) {
                    showStatus("Restock error. Please try again.", true);
                }
                refreshAll();
            }
        }.execute();
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
        // SwingWorker — moves SQLite write off the EDT
        final String productId = p.getProductId();
        final String finalRemarks = remarks;
        final int finalAmount = amount;
        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return service.reduceStockProduct(productId, finalAmount, finalRemarks);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        showStatus(service.getLastMessage(), false);
                        remarksField.setText("");
                    } else {
                        showStatus(service.getLastMessage(), true);
                    }
                } catch (Exception ex) {
                    showStatus("Reduce error. Please try again.", true);
                }
                refreshAll();
            }
        }.execute();
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
        // SwingWorker — moves SQLite undo write off the EDT
        // Command pattern analogue — undoLastRestock() reverses the last AdminActivityLog entry
        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return service.undoLastRestock();
            }
            @Override
            protected void done() {
                try {
                    showStatus(service.getLastMessage(), !get());
                } catch (Exception ex) {
                    showStatus("Undo error. Please try again.", true);
                }
                refreshAll();
            }
        }.execute();
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
        ShopEaseUIUtils.styleDarkButton(undoBtn, new Color(180, 140, 0));
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(231, 76, 60) : new Color(46, 204, 113));
    }
    // Note: button styling is now handled by ShopEaseUIUtils.styleDarkButton()
    // and ShopEaseUIUtils.styleDarkSecondaryButton() — no private duplicates needed.

    private static class AdminProductCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product p) {
                int stock = p.getStockQuantity();
                String status = InventoryStockStatus.label(stock);
                setText(p.getName() + "  ·  Stock: " + stock + "  ·  " + status);
                setBorder(new EmptyBorder(10, 10, 10, 10));
                if (!isSelected) {
                    if (InventoryStockStatus.isOutOfStock(stock)) {
                        setForeground(new Color(231, 76, 60));
                    } else if (InventoryStockStatus.isLowStock(stock)) {
                        setForeground(new Color(241, 196, 15));
                    } else {
                        setForeground(new Color(46, 204, 113));
                    }
                }
            }
            return this;
        }
    }
}
