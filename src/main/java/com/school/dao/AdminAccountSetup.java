
package com.school.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.GeneralSecurityException;

public class AdminAccountSetup {

    public static void main(String[] args) {

        String username = "admin";
        String password = "admin123";
        String fullName = "System Administrator";

        String sql = "INSERT INTO users " +
                "(username, password, full_name, role, status) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(username) DO UPDATE SET " +
                "password = excluded.password, " +
                "full_name = excluded.full_name, " +
                "role = excluded.role, " +
                "status = excluded.status";

        try {
            String hashedPassword = PasswordUtil.hash(password);

            try (Connection connection =
                         DatabaseConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, hashedPassword);
                statement.setString(3, fullName);
                statement.setString(4, "HOSPITAL_ADMINISTRATOR");
                statement.setString(5, "APPROVED");

                statement.executeUpdate();

                System.out.println("================================");
                System.out.println("ADMIN ACCOUNT CREATED!");
                System.out.println("================================");
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
                System.out.println("Role: HOSPITAL_ADMINISTRATOR");
                System.out.println("Status: APPROVED");
                System.out.println("================================");
            }

        } catch (SQLException | GeneralSecurityException e) {
            System.out.println("Admin setup failed: " + e.getMessage());
        }
    }
}
