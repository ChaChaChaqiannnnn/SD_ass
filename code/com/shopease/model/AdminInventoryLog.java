package com.shopease.model;

import java.util.Date;

public class AdminInventoryLog {
    private String adminId;
    private String adminName;
    private String productId;
    private String productName;
    private String actionType;
    private int quantityChange;
    private int stockBefore;
    private int stockAfter;
    private String remarks;
    private Date timestamp;

    public AdminInventoryLog(String adminId, String adminName, String productId, String productName,
                             String actionType, int quantityChange, int stockBefore, int stockAfter,
                             String remarks, Date timestamp) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.productId = productId;
        this.productName = productName;
        this.actionType = actionType;
        this.quantityChange = quantityChange;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
        this.remarks = remarks != null ? remarks.trim() : "";
        this.timestamp = timestamp != null ? timestamp : new Date();
    }

    public String getAdminId() { return adminId; }
    public String getAdminName() { return adminName; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getActionType() { return actionType; }
    public int getQuantityChange() { return quantityChange; }
    public int getStockBefore() { return stockBefore; }
    public int getStockAfter() { return stockAfter; }
    public String getRemarks() { return remarks; }
    public Date getTimestamp() { return timestamp; }

    public String getSummaryLine() {
        String change = quantityChange >= 0 ? "+" + quantityChange : String.valueOf(quantityChange);
        String line = String.format("%s — %s %s (stock %d → %d)",
                actionType, productName, change, stockBefore, stockAfter);
        if (remarks != null && !remarks.isEmpty()) {
            line += " | Remark: " + remarks;
        }
        return line;
    }
}
