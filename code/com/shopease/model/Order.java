package com.shopease.model;

import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private Date orderDate;
    private double totalAmount;
    private String status;
    private List<CartItem> items;

    public Order(String orderId, double totalAmount) {
        this.orderId = orderId;
        this.orderDate = new Date();
        this.totalAmount = totalAmount;
        this.status = "Pending";
    }

    public Order(String orderId, double totalAmount, String status, Date orderDate) {
        this.orderId = orderId;
        this.orderDate = orderDate != null ? orderDate : new Date();
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public Date getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public List<CartItem> getItems() { return items; }

    public void setStatus(String status) { this.status = status; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
