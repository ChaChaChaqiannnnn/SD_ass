package com.shopease.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:shopease.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Users Table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "role TEXT NOT NULL" +
                    ");";
            stmt.execute(createUsersTable);

            // Create Products Table
            String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "stock_quantity INTEGER NOT NULL," +
                    "category TEXT NOT NULL" +
                    ");";
            stmt.execute(createProductsTable);

            // Create Orders Table
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id TEXT PRIMARY KEY," +
                    "user_id TEXT NOT NULL," +
                    "total_amount REAL NOT NULL," +
                    "status TEXT NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createOrdersTable);

            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}
