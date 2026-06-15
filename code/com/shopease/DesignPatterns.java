package com.shopease;

/**
 * Quick reference — GoF pattern roles mapped to ShopEase classes.
 */
public final class DesignPatterns {

    private DesignPatterns() {}

    /** GoF Observer — Subject, Observer, ConcreteSubject, ConcreteObserver. */
    public static final class Observer {
        public static final String SUBJECT = "Subject";
        public static final String CONCRETE_SUBJECT = "ShopEaseInventorySubject";
        public static final String OBSERVER = "Observer";
        public static final String ABSTRACT_OBSERVER = "ShopEaseAbstractObserver";
        public static final String SUBJECT_STATE = "SubjectState";
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

    /** GoF Singleton — private constructor + static instance + getInstance(). */
    public static final class Singleton {
        public static final String DATABASE = "ShopEaseDatabaseManager";
        public static final String CART = "ShopEaseCartSingleton";
        public static final String WISHLIST = "ShopEaseWishlistSingleton";
        private Singleton() {}
    }

    /** GoF Strategy — Context, Strategy, ConcreteStrategy (pure Strategy, no Factory). */
    public static final class Strategy {
        public static final String PAYMENT_STRATEGY = "ShopEasePaymentStrategy";
        public static final String PAYMENT_CONTEXT = "ShopEasePaymentContext";
        public static final String PAYMENT_CONCRETE = "ShopEaseCreditCardStrategy, ShopEaseDuitNowStrategy, ShopEaseMAEStrategy, ShopEaseTNGStrategy";
        public static final String INVENTORY_STRATEGY = "ShopEaseInventoryActionStrategy";
        public static final String INVENTORY_CONTEXT = "ShopEaseInventoryActionContext";
        public static final String INVENTORY_CONCRETE = "RestockInventoryStrategy, ReduceInventoryStrategy, UndoInventoryStrategy";
        private Strategy() {}
    }
}
