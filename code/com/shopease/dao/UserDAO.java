package com.shopease.dao;

import com.shopease.model.Admin;
import com.shopease.model.Customer;
import com.shopease.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean insertUser(User user) {
        String sql = "INSERT INTO users(id, name, email, password, role) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail().trim().toLowerCase());
            pstmt.setString(4, user.getPassword());
            pstmt.setString(5, user instanceof Admin ? "ADMIN" : "CUSTOMER");
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    public String lastInsertErrorKind(User user) {
        if (getUserById(user.getUserId()) != null) {
            return "id";
        }
        if (getUserByEmail(user.getEmail()) != null) {
            return "email";
        }
        return "unknown";
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public boolean deleteUser(String userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement oi = conn.prepareStatement(
                    "DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id = ?)")) {
                oi.setString(1, userId);
                oi.executeUpdate();
            }
            try (PreparedStatement o = conn.prepareStatement("DELETE FROM orders WHERE user_id = ?")) {
                o.setString(1, userId);
                o.executeUpdate();
            }
            try (PreparedStatement w = conn.prepareStatement("DELETE FROM wishlist WHERE user_id = ?")) {
                w.setString(1, userId);
                w.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement("DELETE FROM wishlist_stock_snapshot WHERE user_id = ?")) {
                s.setString(1, userId);
                s.executeUpdate();
            }
            try (PreparedStatement u = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                u.setString(1, userId);
                return u.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserProfile(String userId, String name, String email, String password) {
        String sql = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean emailUsedByOtherUser(String email, String excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email == null ? "" : email.trim());
            pstmt.setString(2, excludeUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        return false;
    }

    public User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by id: " + e.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email == null ? "" : email.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");
        if ("ADMIN".equals(role)) {
            return new Admin(id, name, email, password);
        }
        return new Customer(id, name, email, password);
    }
}
