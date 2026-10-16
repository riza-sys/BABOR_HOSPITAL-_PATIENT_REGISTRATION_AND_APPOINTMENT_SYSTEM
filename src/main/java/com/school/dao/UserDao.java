package com.school.dao;

import com.school.model.User;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {

    public boolean register(String username, String password, String role)
            throws SQLException, GeneralSecurityException {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, 'PENDING')";
        String passwordHash = PasswordUtil.hash(password);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role);
            statement.executeUpdate();
            return true;
        }
    }

    public Optional<User> login(String username, String password)
            throws SQLException, GeneralSecurityException {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ?";

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

                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        storedHash,
                        resultSet.getString("roles"),
                        resultSet.getString("status")
                );
                return Optional.of(user);
            }
        }
    }
}
