package com.school.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:users.db";

    private DatabaseConnection() {
    }

    /**
     * Opens a new connection with SQLite foreign key enforcement turned on.
     * SQLite disables foreign keys by default and the setting is per-connection,
     * so the PRAGMA must run on every connection we hand out.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }
}
