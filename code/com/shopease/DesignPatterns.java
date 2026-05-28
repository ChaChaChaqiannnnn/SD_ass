package com.shopease;

/**
 * Quick reference for your presentation — lists every class that implements a pattern.
 * Open this file first when the lecturer asks "where is Observer / Singleton / Strategy?"
 */
public final class DesignPatterns {

    private DesignPatterns() {}

    /** Observer — subject + function observers in {@code com.shopease.observer}. */
    public static final class Observer {
        public static final String SUBJECT = "ShopEaseInventorySubject";
        public static final String INVENTORY_ADMIN_LOG_OBSERVER = "ShopEaseInventoryAdminLogObserver";
        public static final String INVENTORY_ADMIN_ALERT_OBSERVER = "ShopEaseInventoryAdminAlertObserver";
        public static final String ADMIN_LOGIN_STOCK_ALERT_OBSERVER = "ShopEaseAdminLoginStockAlertObserver";
        public static final String CUSTOMER_CART_REMINDER_OBSERVER = "ShopEaseCustomerCartReminderObserver";
        public static final String CUSTOMER_WISHLIST_RESTOCK_OBSERVER = "ShopEaseCustomerWishlistRestockObserver";
        public static final String DATA_CHANGE_REFRESH_OBSERVER = "ShopEaseDataChangeRefreshObserver";
        public static final String CART_STOCK_SYNC_OBSERVER = "ShopEaseCartStockSyncObserver";
        public static final String APP_EVENTS = "ShopEaseAppEvents";
        private Observer() {}
    }

    /** Singleton — one instance per user or app-wide in {@code com.shopease.singleton}. */
    public static final class Singleton {
        public static final String CART = "ShopEaseCartSingleton";
        public static final String WISHLIST = "ShopEaseWishlistSingleton";
        public static final String DATABASE = "ShopEaseDatabaseManager";
        private Singleton() {}
    }

    /** Strategy — interchangeable algorithms in {@code com.shopease.strategy}. */
    public static final class Strategy {
        public static final String PAYMENT = "ShopEasePaymentStrategy (+ CreditCard, DuitNow, MAE, TNG)";
        public static final String PAYMENT_CONTEXT = "ShopEasePaymentContext";
        public static final String PAYMENT_SELECTOR = "ShopEasePaymentStrategySelector";
        public static final String PROFILE_UPDATE = "CustomerProfileUpdateStrategy";
        public static final String CREATE_CUSTOMER = "CreateCustomerUserStrategy";
        public static final String UPDATE_CUSTOMER = "UpdateCustomerUserStrategy";
        public static final String DELETE_CUSTOMER = "DeleteCustomerUserStrategy";
        public static final String REGISTER_CUSTOMER = "RegisterCustomerStrategy";
        public static final String LOGIN_VALIDATION = "DefaultLoginValidationStrategy";
        public static final String INVENTORY_RESTOCK = "RestockInventoryStrategy";
        public static final String INVENTORY_REDUCE = "ReduceInventoryStrategy";
        public static final String INVENTORY_UNDO = "UndoInventoryStrategy";
        private Strategy() {}
    }
}
