package com.shopease.observer;

/**
 * Observer Pattern — application-wide event names published by {@link ShopEaseInventorySubject}.
 * UI observers subscribe once and react to the events they care about.
 */
public final class ShopEaseAppEvents {
    public static final String DATA_CHANGED = "DATA_CHANGED";
    public static final String LOW_STOCK = "LOW_STOCK";
    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String ADMIN_LOGIN_STOCK = "ADMIN_LOGIN_STOCK";
    public static final String CART_REMINDER = "CART_REMINDER";
    public static final String WISHLIST_RESTOCK = "WISHLIST_RESTOCK";

    private ShopEaseAppEvents() {}
}
