package com.shopease.dao;

import com.shopease.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    public boolean add(String userId, String productId) {
        String sql = "INSERT OR IGNORE INTO wishlist(user_id, product_id) VALUES(?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to wishlist: " + e.getMessage());
            return false;
        }
    }

    public boolean remove(String userId, String productId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing from wishlist: " + e.getMessage());
            return false;
        }
    }

    public boolean contains(String userId, String productId) {
        String sql = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking wishlist: " + e.getMessage());
        }
        return false;
    }

    public List<Product> getProductsForUser(String userId, ProductDAO productDAO) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id FROM wishlist WHERE user_id = ? ORDER BY rowid DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = productDAO.getProductById(rs.getString("product_id"));
                    if (p != null) {
                        products.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching wishlist: " + e.getMessage());
        }
        return products;
    }

    /** Last known stock when customer logged out / previous login; -1 if never recorded. */
    public int getLastKnownStock(String userId, String productId) {
        String sql = "SELECT stock_quantity FROM wishlist_stock_snapshot WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading wishlist snapshot: " + e.getMessage());
        }
        return -1;
    }

    public void saveStockSnapshot(String userId, String productId, int stockQuantity) {
        String sql = "INSERT INTO wishlist_stock_snapshot(user_id, product_id, stock_quantity) VALUES(?,?,?) "
                + "ON CONFLICT(user_id, product_id) DO UPDATE SET stock_quantity = excluded.stock_quantity";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            pstmt.setInt(3, stockQuantity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving wishlist snapshot: " + e.getMessage());
        }
    }

    public void clearSnapshotsForUser(String userId) {
        String sql = "DELETE FROM wishlist_stock_snapshot WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing wishlist snapshots: " + e.getMessage());
        }
    }
}
