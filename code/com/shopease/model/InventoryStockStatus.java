package com.shopease.model;

/**
 * Stock colour rules for the admin inventory list:
 * RED = 0 (NO STOCK), YELLOW = 1–4 (LOW STOCK), GREEN = 5+ (OK).
 * Threshold of 5 is shared with ShopEaseInventorySubject LOW_STOCK events.
 */
public final class InventoryStockStatus {
    public static final int LOW_STOCK_THRESHOLD = 5;

    private InventoryStockStatus() {}

    public static String label(int stock) {
        if (stock <= 0) {
            return "NO STOCK";
        }
        if (stock < LOW_STOCK_THRESHOLD) {
            return "LOW STOCK";
        }
        return "OK";
    }

    public static boolean isOutOfStock(int stock) {
        return stock <= 0;
    }

    public static boolean isLowStock(int stock) {
        return stock > 0 && stock < LOW_STOCK_THRESHOLD;
    }

    public static boolean isOk(int stock) {
        return stock >= LOW_STOCK_THRESHOLD;
    }
}
