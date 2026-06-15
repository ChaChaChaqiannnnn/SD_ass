package com.shopease.observer;

/**
 * GoF Observer — state held by {@link Subject} / {@link ShopEaseInventorySubject}
 * ({@code subjectState}). Concrete observers read this via {@link Subject#getState()}.
 */
public final class SubjectState {
    private static final SubjectState EMPTY = new SubjectState("", "", 0);

    private final String event;
    private final String productName;
    private final int stockQuantity;

    public SubjectState(String event, String productName, int stockQuantity) {
        this.event = event == null ? "" : event;
        this.productName = productName == null ? "" : productName;
        this.stockQuantity = stockQuantity;
    }

    public static SubjectState empty() {
        return EMPTY;
    }

    public String getEvent() {
        return event;
    }

    public String getProductName() {
        return productName;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}
