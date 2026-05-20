package com.shopease.model;

public class Product {
    private String productId;
    private String name;
    private double price;
    private int stockQuantity;
    private String category;

    public Product(String productId, String name, double price, int stockQuantity, String category) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategory() { return category; }
}
