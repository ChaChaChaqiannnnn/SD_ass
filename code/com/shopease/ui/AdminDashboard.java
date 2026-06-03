package com.shopease.ui;

import com.shopease.observer.ShopEaseInventoryAdminAlertObserver;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseDataChangeRefreshObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin home screen — top navigation switches between Inventory and Manage Users.
 * <p>
 * Observer Pattern — attaches {@link com.shopease.observer.ShopEaseDataChangeRefreshObserver}
 * and {@link com.shopease.observer.ShopEaseInventoryAdminAlertObserver}; detaches on logout.
 * Child panels call {@link ShopEaseService} only (Strategy + Observer live in the service layer).
 */
public class AdminDashboard extends JPanel {
    private static final String CARD_INVENTORY = "inventory";
    private static final String CARD_USERS = "users";

    private final ShopEaseService service;
    private final Runnable onLogout;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final AdminInventoryPanel inventoryPanel;
    private final AdminUsersPanel usersPanel;
    private final ShopEaseInventoryObserver uiRefreshObserver;
    private final ShopEaseInventoryObserver adminAlertObserver;
    private JButton inventoryNavBtn;
    private JButton usersNavBtn;

    public AdminDashboard(ShopEaseService service, Runnable onLogout) {
        this.service = service;
        this.inventoryPanel = new AdminInventoryPanel(service);
        this.usersPanel = new AdminUsersPanel(service);
        this.uiRefreshObserver = new ShopEaseDataChangeRefreshObserver(this::refreshActivePanels);
        this.adminAlertObserver = new ShopEaseInventoryAdminAlertObserver(this);
        service.attachObserver(uiRefreshObserver);
        service.attachObserver(adminAlertObserver);
        this.onLogout = () -> {
            service.detachObserver(uiRefreshObserver);
            service.detachObserver(adminAlertObserver);
            onLogout.run();
        };

        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));

        add(buildTopHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(4, 10, 10, 10));
        JScrollPane inventoryScroll = new JScrollPane(inventoryPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inventoryScroll.setBorder(null);
        inventoryScroll.setOpaque(false);
        inventoryScroll.getViewport().setOpaque(false);
        contentPanel.add(inventoryScroll, CARD_INVENTORY);
        contentPanel.add(usersPanel, CARD_USERS);
        add(contentPanel, BorderLayout.CENTER);

        showSection(CARD_INVENTORY);
    }

    private JPanel buildTopHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(new Color(44, 62, 80));
        header.setBorder(new EmptyBorder(6, 12, 2, 12));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel welcome = new JLabel("ShopEase Admin");
        welcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        welcome.setForeground(new Color(236, 240, 241));
        titleRow.add(welcome, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        ShopEaseUIUtils.styleDarkButton(logoutBtn, new Color(192, 57, 43));
        compactHeaderButton(logoutBtn);
        logoutBtn.addActionListener(e -> {
            if (ShopEaseUIUtils.confirmLogout(this)) {
                service.logout();
                onLogout.run();
            }
        });
        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoutWrap.setOpaque(false);
        logoutWrap.add(logoutBtn);
        titleRow.add(logoutWrap, BorderLayout.EAST);
        header.add(titleRow, BorderLayout.NORTH);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(2, 0, 4, 0));

        inventoryNavBtn = navButton("Inventory management");
        inventoryNavBtn.addActionListener(e -> showSection(CARD_INVENTORY));
        usersNavBtn = navButton("Manage users");
        usersNavBtn.addActionListener(e -> showSection(CARD_USERS));

        navBar.add(inventoryNavBtn);
        navBar.add(usersNavBtn);
        header.add(navBar, BorderLayout.CENTER);

        JSeparator line = new JSeparator();
        line.setForeground(new Color(127, 140, 141));
        header.add(line, BorderLayout.SOUTH);

        return header;
    }

    private static void compactHeaderButton(JButton btn) {
        btn.setFont(ShopEaseUIUtils.smallFont());
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
    }

    private JButton navButton(String text) {
        JButton btn = new JButton(text);
        ShopEaseUIUtils.styleNavButton(btn);
        compactHeaderButton(btn);
        return btn;
    }

    private void showSection(String card) {
        contentLayout.show(contentPanel, card);
        styleNav(inventoryNavBtn, CARD_INVENTORY.equals(card));
        styleNav(usersNavBtn, CARD_USERS.equals(card));
        if (CARD_INVENTORY.equals(card)) {
            inventoryPanel.refreshAll();
        } else {
            usersPanel.refreshAll();
        }
    }

    private void styleNav(JButton btn, boolean active) {
        if (active) {
            ShopEaseUIUtils.styleDarkButton(btn, new Color(52, 152, 219));
        } else {
            ShopEaseUIUtils.styleNavButton(btn);
        }
        compactHeaderButton(btn);
    }

    private void refreshActivePanels() {
        inventoryPanel.refreshAll();
        usersPanel.refreshAll();
    }
}
