package com.shopease.model;

import java.util.Date;

public class Order {
    private String orderId;
    private Date orderDate;
    private double totalAmount;
    private String status;

    public Order(String orderId, double totalAmount) {
        this.orderId = orderId;
        this.orderDate = new Date();
        this.totalAmount = totalAmount;
        this.status = "Pending";
    }

    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public void setStatus(String status) { this.status = status; }
}
