package com.shopease.dao;

import com.shopease.model.CartItem;
import com.shopease.model.Order;
import com.shopease.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDAO {

    public void insertOrder(Order order, String userId) {
        String sql = "INSERT INTO orders(order_id, user_id, total_amount, status, order_date) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, order.getOrderId());
            pstmt.setString(2, userId);
            pstmt.setDouble(3, order.getTotalAmount());
            pstmt.setString(4, order.getStatus());
            pstmt.setLong(5, order.getOrderDate().getTime());
            pstmt.executeUpdate();
            insertOrderItems(order);
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
        }
    }

    private void insertOrderItems(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO order_items(order_id, product_id, product_name, quantity, unit_price) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (CartItem item : order.getItems()) {
                pstmt.setString(1, order.getOrderId());
                pstmt.setString(2, item.getProduct().getProductId());
                pstmt.setString(3, item.getProduct().getName());
                pstmt.setInt(4, item.getQuantity());
                pstmt.setDouble(5, item.getProduct().getPrice());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error inserting order items: " + e.getMessage());
        }
    }

    public List<Order> getOrdersByUserId(String userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, total_amount, status, order_date FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String orderId = rs.getString("order_id");
                    double total = rs.getDouble("total_amount");
                    String status = rs.getString("status");
                    long orderDateMs = rs.getLong("order_date");
                    Date orderDate = orderDateFromRow(orderId, orderDateMs);
                    Order order = new Order(orderId, total, status, orderDate);
                    order.setItems(getOrderItems(orderId));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }

    private List<CartItem> getOrderItems(String orderId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT product_id, product_name, quantity, unit_price FROM order_items WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getString("product_id"),
                            rs.getString("product_name"),
                            rs.getDouble("unit_price"),
                            0,
                            ""
                    );
                    items.add(new CartItem(p, rs.getInt("quantity")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order items: " + e.getMessage());
        }
        return items;
    }

    private Date orderDateFromRow(String orderId, long orderDateMs) {
        if (orderDateMs > 0) {
            return new Date(orderDateMs);
        }
        if (orderId != null && orderId.startsWith("ORD-")) {
            try {
                return new Date(Long.parseLong(orderId.substring(4)));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return new Date();
    }
}
