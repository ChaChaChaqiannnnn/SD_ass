package com.shopease.dao;

import com.shopease.model.AdminInventoryLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminActivityDAO {

    public void insertLog(AdminInventoryLog log) {
        String sql = "INSERT INTO admin_inventory_log(admin_id, admin_name, product_id, product_name, "
                + "action_type, quantity_change, stock_before, stock_after, remarks, created_at) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.getAdminId());
            pstmt.setString(2, log.getAdminName());
            pstmt.setString(3, log.getProductId());
            pstmt.setString(4, log.getProductName());
            pstmt.setString(5, log.getActionType());
            pstmt.setInt(6, log.getQuantityChange());
            pstmt.setInt(7, log.getStockBefore());
            pstmt.setInt(8, log.getStockAfter());
            pstmt.setString(9, log.getRemarks());
            pstmt.setLong(10, log.getTimestamp().getTime());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting admin log: " + e.getMessage());
        }
    }

    public List<AdminInventoryLog> getLogsForAdmin(String adminId, int limit) {
        List<AdminInventoryLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM admin_inventory_log WHERE admin_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching admin logs: " + e.getMessage());
        }
        return logs;
    }

    private AdminInventoryLog mapRow(ResultSet rs) throws SQLException {
        String remarks = "";
        try {
            remarks = rs.getString("remarks");
            if (remarks == null) {
                remarks = "";
            }
        } catch (SQLException ignored) {
            // older DB without remarks column
        }
        return new AdminInventoryLog(
                rs.getString("admin_id"),
                rs.getString("admin_name"),
                rs.getString("product_id"),
                rs.getString("product_name"),
                rs.getString("action_type"),
                rs.getInt("quantity_change"),
                rs.getInt("stock_before"),
                rs.getInt("stock_after"),
                remarks,
                new Date(rs.getLong("created_at"))
        );
    }
}
