package com.shopease.ui;

import com.shopease.model.AdminInventoryLog;
import com.shopease.model.InventoryStockStatus;
import com.shopease.model.Product;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Admin sidebar — restock, reduce (with remarks), undo, and coloured stock list.
 * Stock changes trigger Observer events so other screens refresh too.
 * <p>
 * Observer Pattern — all stock writes go through {@link ShopEaseService}, which publishes
 * LOW_STOCK / OUT_OF_STOCK / DATA_CHANGED to attached observers.
 */
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
        styleUndoButton(undoBtn);
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
        remarksField.setToolTipText("Required — explain why stock is being reduced");
        remarksField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141)),
                new EmptyBorder(8, 10, 8, 10)));
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
        // SwingWorker — SQLite stock update off the EDT; Observer fires via ShopEaseService.restockProduct()
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
        // SwingWorker — SQLite write off the EDT; Observer fires via ShopEaseService.reduceStockProduct()
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
    }

    /** Matches other admin action buttons but paints flat text (no disabled emboss/shadow). */
    private static void styleUndoButton(JButton btn) {
        Color bg = ShopEaseUIUtils.WARNING;
        Font buttonFont = ShopEaseUIUtils.bodyFont();
        btn.setFont(buttonFont);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setUI(new BasicButtonUI() {
            @Override
            protected void installDefaults(AbstractButton b) {
                super.installDefaults(b);
                b.setFont(buttonFont);
                b.setForeground(Color.WHITE);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(undoButtonFill(b, bg));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                g2.dispose();

                String text = b.getText();
                if (text == null || text.isEmpty()) {
                    return;
                }
                Graphics2D tg = (Graphics2D) g.create();
                tg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                tg.setFont(buttonFont);
                tg.setColor(b.isEnabled() ? Color.WHITE : new Color(255, 255, 255, 170));
                FontMetrics fm = tg.getFontMetrics();
                int textX = (c.getWidth() - fm.stringWidth(text)) / 2;
                int textY = ((c.getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                tg.drawString(text, textX, textY);
                tg.dispose();
            }
        });
    }

    private static Color undoButtonFill(AbstractButton b, Color bg) {
        if (!b.isEnabled()) {
            return new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 140);
        }
        if (b.getModel().isPressed()) {
            return bg.darker();
        }
        if (b.getModel().isRollover()) {
            return new Color(
                    Math.min(255, bg.getRed() + 22),
                    Math.min(255, bg.getGreen() + 22),
                    Math.min(255, bg.getBlue() + 22));
        }
        return bg;
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(231, 76, 60) : new Color(46, 204, 113));
    }

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
