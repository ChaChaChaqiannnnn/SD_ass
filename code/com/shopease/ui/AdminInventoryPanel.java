package com.shopease.ui;

import com.shopease.model.AdminInventoryLog;
import com.shopease.model.InventoryStockStatus;
import com.shopease.model.Product;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Admin inventory — select a product, then add or reduce stock with clear grouped actions.
 * Stock changes trigger Observer events so other screens refresh too.
 */
public class AdminInventoryPanel extends JPanel implements Scrollable {
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");
    private static final Color FIELD_BG = new Color(236, 240, 241);
    private static final Color PANEL_BG = new Color(52, 73, 94);
    private static final Color BORDER = new Color(127, 140, 141);
    private static final Color TEXT = new Color(236, 240, 241);
    private static final Color MUTED = new Color(189, 195, 199);
    private static final int FIELD_HEIGHT = 32;
    private static final int STACK_BREAKPOINT_PX = 720;

    private final ShopEaseService service;
    private DefaultListModel<Product> listModel;
    private JList<Product> productList;
    private JTextArea historyArea;
    private JLabel selectedLabel;
    private JLabel statusLabel;
    private JButton restockBtn;
    private JButton quickTenBtn;
    private JButton reduceBtn;
    private JButton undoBtn;
    private JSpinner restockSpinner;
    private JSpinner reduceSpinner;
    private JTextField remarksField;
    private final JPanel restockCard;
    private final JPanel reduceCard;
    private final JPanel actionCardsHost;

    public AdminInventoryPanel(ShopEaseService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.58);
        split.setOpaque(false);
        split.setBorder(null);
        split.setContinuousLayout(true);

        JPanel productsPanel = buildProductsPanel();
        JPanel historyPanel = buildHistoryPanel();
        productsPanel.setMinimumSize(new Dimension(240, 140));
        historyPanel.setMinimumSize(new Dimension(220, 140));
        split.setLeftComponent(productsPanel);
        split.setRightComponent(historyPanel);

        selectedLabel = new JLabel("No product selected — choose one from the list above.");
        selectedLabel.setFont(ShopEaseUIUtils.bodyFont());
        selectedLabel.setForeground(MUTED);
        selectedLabel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel selectionBar = new JPanel(new BorderLayout());
        selectionBar.setBackground(PANEL_BG);
        selectionBar.setBorder(BorderFactory.createLineBorder(BORDER));
        selectionBar.add(selectedLabel, BorderLayout.CENTER);

        restockCard = buildRestockCard();
        reduceCard = buildReduceCard();

        actionCardsHost = new JPanel();
        actionCardsHost.setOpaque(false);
        actionCardsHost.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                relayoutActionCards();
            }
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ShopEaseUIUtils.smallFont());
        statusLabel.setForeground(MUTED);
        statusLabel.setBorder(new EmptyBorder(6, 2, 0, 2));

        JPanel actionsPanel = new JPanel(new BorderLayout(0, 10));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        actionsPanel.add(selectionBar, BorderLayout.NORTH);
        actionsPanel.add(actionCardsHost, BorderLayout.CENTER);
        actionsPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(split, BorderLayout.CENTER);
        center.add(actionsPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        productList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionState();
            }
        });

        relayoutActionCards();
        refreshAll();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(700, 500);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    /** Fill the viewport normally; only enable horizontal scrolling when viewport is narrower than minimum. */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container parent = getParent();
        if (parent instanceof JViewport vp) {
            return vp.getWidth() >= getMinimumSize().width;
        }
        return true;
    }

    /** Fill the viewport vertically; only enable vertical scrolling when viewport is shorter than minimum. */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container parent = getParent();
        if (parent instanceof JViewport vp) {
            return vp.getHeight() >= getMinimumSize().height;
        }
        return true;
    }

    private JPanel buildProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);
        panel.setBorder(sectionBorder("Products"));

        listModel = new DefaultListModel<>();
        productList = new JList<>(listModel);
        productList.setCellRenderer(new AdminProductCellRenderer());
        productList.setFixedCellHeight(40);
        productList.setFont(ShopEaseUIUtils.bodyFont());
        productList.setBackground(PANEL_BG);
        productList.setForeground(TEXT);
        productList.setSelectionBackground(new Color(52, 152, 219));
        productList.setSelectionForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(productList);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);
        panel.setBorder(sectionBorder("Activity log"));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyArea.setBackground(PANEL_BG);
        historyArea.setForeground(TEXT);
        historyArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton refreshHistoryBtn = new JButton("Refresh");
        compactButton(refreshHistoryBtn, new Color(52, 73, 94));
        refreshHistoryBtn.addActionListener(e -> refreshHistory());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        top.add(refreshHistoryBtn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRestockCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(sectionBorder("Add stock"));

        GridBagConstraints labelC = new GridBagConstraints();
        labelC.anchor = GridBagConstraints.WEST;
        labelC.insets = new Insets(0, 8, 6, 8);
        GridBagConstraints valueC = new GridBagConstraints();
        valueC.anchor = GridBagConstraints.WEST;
        valueC.insets = new Insets(0, 0, 6, 8);
        valueC.fill = GridBagConstraints.HORIZONTAL;
        valueC.weightx = 1;

        restockSpinner = styledSpinner(10);
        restockBtn = new JButton("Add stock");
        compactButton(restockBtn, new Color(39, 174, 96));
        restockBtn.addActionListener(e -> doRestock((int) restockSpinner.getValue()));

        quickTenBtn = new JButton("Quick +10");
        compactButton(quickTenBtn, new Color(52, 73, 94));
        quickTenBtn.addActionListener(e -> doRestock(10));

        undoBtn = new JButton("Undo last change");
        compactButton(undoBtn, ShopEaseUIUtils.WARNING);
        undoBtn.addActionListener(e -> doUndo());

        addFormRow(card, 0, "Quantity to add", restockSpinner, labelC, valueC);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(restockBtn);
        buttonRow.add(quickTenBtn);
        GridBagConstraints btnC = new GridBagConstraints();
        btnC.gridx = 0;
        btnC.gridy = 1;
        btnC.gridwidth = 2;
        btnC.anchor = GridBagConstraints.WEST;
        btnC.insets = new Insets(0, 8, 6, 8);
        btnC.fill = GridBagConstraints.HORIZONTAL;
        btnC.weightx = 1;
        card.add(buttonRow, btnC);

        btnC.gridy = 2;
        btnC.insets = new Insets(0, 8, 8, 8);
        card.add(undoBtn, btnC);

        JLabel hint = mutedHint("Increases stock for the selected product.");
        btnC.gridy = 3;
        btnC.insets = new Insets(0, 8, 4, 8);
        card.add(hint, btnC);
        return card;
    }

    private JPanel buildReduceCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(sectionBorder("Reduce stock"));

        GridBagConstraints labelC = new GridBagConstraints();
        labelC.anchor = GridBagConstraints.WEST;
        labelC.insets = new Insets(0, 8, 6, 8);
        GridBagConstraints valueC = new GridBagConstraints();
        valueC.anchor = GridBagConstraints.WEST;
        valueC.insets = new Insets(0, 0, 6, 8);
        valueC.fill = GridBagConstraints.HORIZONTAL;
        valueC.weightx = 1;

        reduceSpinner = styledSpinner(1);
        remarksField = adminField("e.g. Damaged units, inventory audit");
        remarksField.setPreferredSize(new Dimension(180, FIELD_HEIGHT));
        remarksField.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));

        reduceBtn = new JButton("Reduce stock");
        compactButton(reduceBtn, new Color(192, 57, 43));
        reduceBtn.addActionListener(e -> doReduce());

        addFormRow(card, 0, "Quantity to remove", reduceSpinner, labelC, valueC);
        addFormRow(card, 1, "Reason (required)", remarksField, labelC, valueC);

        GridBagConstraints btnC = new GridBagConstraints();
        btnC.gridx = 0;
        btnC.gridy = 2;
        btnC.gridwidth = 2;
        btnC.anchor = GridBagConstraints.WEST;
        btnC.insets = new Insets(2, 8, 6, 8);
        card.add(reduceBtn, btnC);

        JLabel hint = mutedHint("Reason is saved in the activity log.");
        btnC.gridy = 3;
        btnC.insets = new Insets(0, 8, 4, 8);
        card.add(hint, btnC);
        return card;
    }

    /** Side-by-side on wide screens; stacked on narrow screens so controls never clip. */
    private void relayoutActionCards() {
        actionCardsHost.removeAll();
        int width = actionCardsHost.getWidth();
        if (width <= 0) {
            width = getWidth() > 0 ? getWidth() : STACK_BREAKPOINT_PX;
        }

        if (width < STACK_BREAKPOINT_PX) {
            actionCardsHost.setLayout(new BoxLayout(actionCardsHost, BoxLayout.Y_AXIS));
            restockCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            reduceCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            restockCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, restockCard.getPreferredSize().height + 20));
            reduceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, reduceCard.getPreferredSize().height + 20));
            actionCardsHost.add(restockCard);
            actionCardsHost.add(Box.createVerticalStrut(8));
            actionCardsHost.add(reduceCard);
        } else {
            actionCardsHost.setLayout(new GridBagLayout());
            GridBagConstraints left = new GridBagConstraints();
            left.gridx = 0;
            left.gridy = 0;
            left.weightx = 0.5;
            left.weighty = 0;
            left.fill = GridBagConstraints.BOTH;
            left.insets = new Insets(0, 0, 0, 6);
            GridBagConstraints right = new GridBagConstraints();
            right.gridx = 1;
            right.gridy = 0;
            right.weightx = 0.5;
            right.weighty = 0;
            right.fill = GridBagConstraints.BOTH;
            actionCardsHost.add(restockCard, left);
            actionCardsHost.add(reduceCard, right);
        }
        actionCardsHost.revalidate();
        actionCardsHost.repaint();
    }

    public void refreshAll() {
        refreshList();
        refreshHistory();
        updateSelectionState();
    }

    private Product getSelectedProduct() {
        return productList.getSelectedValue();
    }

    private void updateSelectionState() {
        Product p = getSelectedProduct();
        if (p == null) {
            selectedLabel.setText("No product selected — choose one from the list above.");
            selectedLabel.setForeground(MUTED);
            restockBtn.setEnabled(false);
            quickTenBtn.setEnabled(false);
            reduceBtn.setEnabled(false);
        } else {
            int stock = p.getStockQuantity();
            String status = InventoryStockStatus.label(stock);
            selectedLabel.setText("Selected: " + p.getName() + "   ·   Stock: " + stock + "   ·   " + status);
            if (InventoryStockStatus.isOutOfStock(stock)) {
                selectedLabel.setForeground(new Color(231, 76, 60));
            } else if (InventoryStockStatus.isLowStock(stock)) {
                selectedLabel.setForeground(new Color(241, 196, 15));
            } else {
                selectedLabel.setForeground(new Color(46, 204, 113));
            }
            restockBtn.setEnabled(true);
            quickTenBtn.setEnabled(true);
            reduceBtn.setEnabled(true);
        }
        undoBtn.setEnabled(service.canUndoLastRestock());
    }

    private void doRestock(int amount) {
        Product p = getSelectedProduct();
        if (p == null) {
            showStatus("Select a product from the list first.", true);
            return;
        }
        final String productId = p.getProductId();
        new SwingWorker<Boolean, Void>() {
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
            showStatus("Enter a reason before reducing stock.", true);
            remarksField.requestFocus();
            return;
        }
        int amount = (int) reduceSpinner.getValue();
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Remove <b>" + amount + "</b> from <b>" + p.getName() + "</b>?<br><br>"
                        + "Reason: " + remarks + "</html>",
                "Confirm stock reduction",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final String productId = p.getProductId();
        final String finalRemarks = remarks;
        final int finalAmount = amount;
        new SwingWorker<Boolean, Void>() {
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
                "Revert your last stock change for this session?",
                "Confirm undo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<Boolean, Void>() {
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
        Product selected = getSelectedProduct();
        String selectedId = selected != null ? selected.getProductId() : null;
        listModel.clear();
        int reselect = -1;
        List<Product> products = service.getAllProducts();
        for (int i = 0; i < products.size(); i++) {
            listModel.addElement(products.get(i));
            if (selectedId != null && selectedId.equals(products.get(i).getProductId())) {
                reselect = i;
            }
        }
        if (reselect >= 0) {
            productList.setSelectedIndex(reselect);
        }
    }

    private void refreshHistory() {
        List<AdminInventoryLog> logs = service.getAdminActivityHistory();
        if (logs.isEmpty()) {
            historyArea.setText("No activity yet.\nRestocks, reductions, and undos appear here.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (AdminInventoryLog log : logs) {
            sb.append(TIME_FMT.format(log.getTimestamp()))
                    .append("  ")
                    .append(log.getSummaryLine())
                    .append('\n');
        }
        historyArea.setText(sb.toString());
        historyArea.setCaretPosition(0);
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(231, 76, 60) : new Color(46, 204, 113));
    }

    private static TitledBorder sectionBorder(String title) {
        return new TitledBorder(
                BorderFactory.createLineBorder(BORDER),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                ShopEaseUIUtils.smallFont(), TEXT);
    }

    private static void addFormRow(JPanel form, int row, String label, JComponent value,
                                   GridBagConstraints labelC, GridBagConstraints valueC) {
        JLabel l = new JLabel(label);
        l.setFont(ShopEaseUIUtils.smallFont());
        l.setForeground(MUTED);
        labelC.gridx = 0;
        labelC.gridy = row;
        valueC.gridx = 1;
        valueC.gridy = row;
        form.add(l, labelC);
        form.add(value, valueC);
    }

    private static JLabel mutedHint(String text) {
        JLabel hint = new JLabel(text);
        hint.setFont(ShopEaseUIUtils.smallFont());
        hint.setForeground(MUTED);
        return hint;
    }

    private static void compactButton(JButton btn, Color bg) {
        if (new Color(52, 73, 94).equals(bg)) {
            ShopEaseUIUtils.styleDarkSecondaryButton(btn);
        } else {
            ShopEaseUIUtils.styleDarkButton(btn, bg);
        }
        btn.setFont(ShopEaseUIUtils.smallFont());
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
    }

    private static JSpinner styledSpinner(int initial) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 1, 9999, 1));
        styleSpinnerEditor(spinner);
        spinner.setPreferredSize(new Dimension(100, FIELD_HEIGHT));
        return spinner;
    }

    /** Flat editor styling — avoids macOS ghost/shadow text when typing in the spinner. */
    private static void styleSpinnerEditor(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JTextField field = defaultEditor.getTextField();
            field.setFont(ShopEaseUIUtils.bodyFont());
            field.setBackground(FIELD_BG);
            field.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
            field.setOpaque(true);
            field.setHorizontalAlignment(SwingConstants.RIGHT);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(4, 8, 4, 8)));
            field.setUI(new BasicTextFieldUI());
            defaultEditor.setBackground(FIELD_BG);
            defaultEditor.setOpaque(true);
        }
        spinner.setBorder(null);
    }

    private static JTextField adminField(String tooltip) {
        JTextField field = new JTextField();
        field.setFont(ShopEaseUIUtils.bodyFont());
        field.setBackground(FIELD_BG);
        field.setForeground(ShopEaseUIUtils.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        if (tooltip != null && !tooltip.isEmpty()) {
            field.setToolTipText(tooltip);
        }
        return field;
    }

    private static class AdminProductCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product p) {
                int stock = p.getStockQuantity();
                setText(p.getName() + "   ·   " + stock + " in stock   ·   " + InventoryStockStatus.label(stock));
                setBorder(new EmptyBorder(8, 10, 8, 10));
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
