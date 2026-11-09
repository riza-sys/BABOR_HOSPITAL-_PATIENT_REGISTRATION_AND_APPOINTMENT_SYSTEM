
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

    // REGISTER USER
    public boolean register(String username, String password,
                            String fullName, Role role)
            throws SQLException, GeneralSecurityException {

        String sql = "INSERT INTO users " +
                "(username, password, full_name, role, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, PasswordUtil.hash(password));
            statement.setString(3, fullName);
            statement.setString(4, role.name());
            statement.setString(5, "PENDING");

            return statement.executeUpdate() > 0;
        }
    }

    // LOGIN USER
    public Optional<User> login(String username, String password)
            throws SQLException, GeneralSecurityException {

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    String hashedPassword = rs.getString("password");

                    if (PasswordUtil.verify(password, hashedPassword)) {

                        User user = new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                Role.valueOf(rs.getString("role")),
                                rs.getString("status")
                        );

                        return Optional.of(user);
                    }
                }
            }
        }

        return Optional.empty();
    }

    // GET ALL USERS
    public List<User> getAllUsers() throws SQLException {

        String sql = "SELECT * FROM users ORDER BY user_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            return mapUsers(rs);
        }
    }

    // GET PENDING USERS
    public List<User> getPendingUsers() throws SQLException {

        String sql = "SELECT * FROM users " +
                "WHERE status = ? ORDER BY user_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "PENDING");

            try (ResultSet rs = statement.executeQuery()) {
                return mapUsers(rs);
            }
        }
    }

    // APPROVE USER
    public boolean approveUser(int userId) throws SQLException {

        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "APPROVED");
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    // UPDATE USER ROLE AND STATUS
    public boolean updateUserRoleAndStatus(
            int userId, String newRole, String newStatus)
            throws SQLException {

        String sql = "UPDATE users SET role = ?, status = ? " +
                "WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newRole.toUpperCase());
            statement.setString(2, newStatus.toUpperCase());
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;
        }
    }

    // MAP DATABASE USERS
    private List<User> mapUsers(ResultSet rs) throws SQLException {

        List<User> users = new ArrayList<>();

        while (rs.next()) {

            users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    Role.valueOf(rs.getString("role")),
                    rs.getString("status")
            ));
        }

        return users;
    }
}
