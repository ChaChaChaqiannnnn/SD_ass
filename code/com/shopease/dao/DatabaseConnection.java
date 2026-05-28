package com.shopease.dao;

import com.shopease.singleton.ShopEaseDatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DAO layer entry point for SQLite connections.
 * Delegates to {@link ShopEaseDatabaseManager} (Singleton) so all SQL access shares one manager.
 */
public final class DatabaseConnection {

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return ShopEaseDatabaseManager.getInstance().getConnection();
    }

    public static void initializeDatabase() {
        ShopEaseDatabaseManager.getInstance().initializeDatabase();
    }
}
