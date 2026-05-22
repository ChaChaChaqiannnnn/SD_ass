package com.shopease.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_PATH = System.getProperty("user.dir") + java.io.File.separator + "shopease.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
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
                    "order_date INTEGER NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createOrdersTable);
            try {
                stmt.execute("ALTER TABLE orders ADD COLUMN order_date INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {
                // column already exists
            }

            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "order_id TEXT NOT NULL," +
                    "product_id TEXT NOT NULL," +
                    "product_name TEXT NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                    ");";
            stmt.execute(createOrderItemsTable);

            String createAdminLogTable = "CREATE TABLE IF NOT EXISTS admin_inventory_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "admin_id TEXT NOT NULL," +
                    "admin_name TEXT NOT NULL," +
                    "product_id TEXT NOT NULL," +
                    "product_name TEXT NOT NULL," +
                    "action_type TEXT NOT NULL," +
                    "quantity_change INTEGER NOT NULL," +
                    "stock_before INTEGER NOT NULL," +
                    "stock_after INTEGER NOT NULL," +
                    "created_at INTEGER NOT NULL" +
                    ");";
            stmt.execute(createAdminLogTable);
            try {
                stmt.execute("ALTER TABLE admin_inventory_log ADD COLUMN remarks TEXT NOT NULL DEFAULT ''");
            } catch (SQLException ignored) {
                // column already exists
            }

            String createWishlistTable = "CREATE TABLE IF NOT EXISTS wishlist (" +
                    "user_id TEXT NOT NULL," +
                    "product_id TEXT NOT NULL," +
                    "PRIMARY KEY(user_id, product_id)," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(product_id) REFERENCES products(id)" +
                    ");";
            stmt.execute(createWishlistTable);

            String createWishlistSnapshotTable = "CREATE TABLE IF NOT EXISTS wishlist_stock_snapshot ("
                    + "user_id TEXT NOT NULL,"
                    + "product_id TEXT NOT NULL,"
                    + "stock_quantity INTEGER NOT NULL,"
                    + "PRIMARY KEY(user_id, product_id),"
                    + "FOREIGN KEY(user_id) REFERENCES users(id),"
                    + "FOREIGN KEY(product_id) REFERENCES products(id)"
                    + ");";
            stmt.execute(createWishlistSnapshotTable);

            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}
