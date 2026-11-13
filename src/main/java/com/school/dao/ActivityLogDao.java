package com.school.dao;

import com.school.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Insert a new log entry (never crashes the app if logging fails)
    public void logAction(int userId, String actionType, String description) {

        String sql = "INSERT INTO activity_logs "
                + "(user_id, action_type, description) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Logging error: " + e.getMessage());
        }
    }

    // All logs, newest first
    public List<ActivityLog> getAllLogs() {

        String sql = "SELECT * FROM activity_logs "
                + "ORDER BY timestamp DESC, log_id DESC";

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return logs;
    }

    // Logs for one user, newest first
    public List<ActivityLog> getLogsByUserId(int userId) {

        String sql = "SELECT * FROM activity_logs WHERE user_id = ? "
                + "ORDER BY timestamp DESC, log_id DESC";

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return logs;
    }

    // Delete all logs
    public boolean clearAllLogs() {

        String sql = "DELETE FROM activity_logs";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}