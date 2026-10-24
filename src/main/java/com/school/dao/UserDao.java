package com.school.dao;

import com.school.model.Role;
import com.school.model.User;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    /**
     * Inserts a new account with PENDING status.
     *
     * @return true if the account was created, false if the username is already taken
     */
    public boolean register(String username, String password, String fullName, Role role)
            throws SQLException, GeneralSecurityException {
        String sql = "INSERT INTO users (username, password, role, full_name, status) "
                + "VALUES (?, ?, ?, ?, 'PENDING')";
        String passwordHash = PasswordUtil.hash(password);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role.name());
            statement.setString(4, fullName);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            }
            throw e;
        }
    }

    public Optional<User> login(String username, String password)
            throws SQLException, GeneralSecurityException {
        String sql = "SELECT user_id, username, password, full_name, role, status FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                String storedHash = resultSet.getString("password");
                if (!PasswordUtil.verify(password, storedHash)) {
                    return Optional.empty();
                }

                return Optional.of(new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        storedHash,
                        resultSet.getString("full_name"),
                        Role.valueOf(resultSet.getString("role")),
                        resultSet.getString("status")
                ));
            }
        }
    }

    /**
     * @return every registered account, regardless of role or status
     */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT user_id, username, password, full_name, role, status "
                + "FROM users ORDER BY user_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapUsers(resultSet);
        }
    }

    /**
     * @return every account (all roles) whose status is still PENDING
     */
    public List<User> getPendingUsers() throws SQLException {
        String sql = "SELECT user_id, username, password, full_name, role, status "
                + "FROM users WHERE status = ? ORDER BY user_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "PENDING");

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapUsers(resultSet);
            }
        }
    }

    /**
     * Changes the target account's status to APPROVED.
     *
     * @return true if a row was updated, false if no user has that ID
     */
    public boolean approveUser(int userId) throws SQLException {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private List<User> mapUsers(ResultSet resultSet) throws SQLException {
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("full_name"),
                    Role.valueOf(resultSet.getString("role")),
                    resultSet.getString("status")
            ));
        }
        return users;
    }
}
