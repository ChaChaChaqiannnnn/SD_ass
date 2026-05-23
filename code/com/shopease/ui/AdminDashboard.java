package com.shopease.ui;

import com.shopease.observer.ShopEaseAdminAlertObserver;
import com.shopease.observer.ShopEaseInventoryObserver;
import com.shopease.observer.ShopEaseUiRefreshObserver;
import com.shopease.service.ShopEaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Admin shell with sidebar: Inventory management | Manage users. */
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
        this.uiRefreshObserver = new ShopEaseUiRefreshObserver(this::refreshActivePanels);
        this.adminAlertObserver = new ShopEaseAdminAlertObserver(this);
        service.attachObserver(uiRefreshObserver);
        service.attachObserver(adminAlertObserver);
        this.onLogout = () -> {
            service.detachObserver(uiRefreshObserver);
            service.detachObserver(adminAlertObserver);
            onLogout.run();
        };

        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(16, 20, 12, 20));
        JLabel welcome = new JLabel("ShopEase Admin");
        welcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        welcome.setForeground(new Color(236, 240, 241));
        JLabel sub = new JLabel("Signed in as " + service.getCurrentUser().getName());
        sub.setFont(ShopEaseUIUtils.smallFont());
        sub.setForeground(new Color(189, 195, 199));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(welcome);
        titles.add(sub);
        topBar.add(titles, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 0, 16, 16));

        JPanel sidebar = buildSidebar();
        sidebar.setPreferredSize(new Dimension(220, 0));
        body.add(sidebar, BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 16, 0, 0));
        contentPanel.add(inventoryPanel, CARD_INVENTORY);
        contentPanel.add(usersPanel, CARD_USERS);
        body.add(contentPanel, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        showSection(CARD_INVENTORY);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(52, 73, 94));
        sidebar.setBorder(new EmptyBorder(16, 12, 16, 12));

        JLabel navTitle = new JLabel("MENU");
        navTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        navTitle.setForeground(new Color(149, 165, 166));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navTitle.setBorder(new EmptyBorder(0, 8, 12, 0));
        sidebar.add(navTitle);

        inventoryNavBtn = navButton("Inventory management");
        inventoryNavBtn.addActionListener(e -> showSection(CARD_INVENTORY));
        sidebar.add(inventoryNavBtn);
        sidebar.add(Box.createVerticalStrut(8));

        usersNavBtn = navButton("Manage users");
        usersNavBtn.addActionListener(e -> showSection(CARD_USERS));
        sidebar.add(usersNavBtn);

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        ShopEaseUIUtils.styleDarkButton(logoutBtn, new Color(192, 57, 43));
        logoutBtn.addActionListener(e -> {
            service.logout();
            onLogout.run();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton navButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        ShopEaseUIUtils.styleNavButton(btn);
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
    }

    private void refreshActivePanels() {
        inventoryPanel.refreshAll();
        usersPanel.refreshAll();
    }
}
