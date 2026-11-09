
package com.school.ui;

import com.school.dao.UserDao;
import com.school.model.User;
import com.school.model.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Menu for the Hospital Administrator.
 * Allows viewing, approving, and updating user accounts.
 */
public class AdminMenu extends RoleMenu {

    private static final int ID_WIDTH = 5;
    private static final int USERNAME_WIDTH = 15;
    private static final int ROLE_WIDTH = 22;
    private static final int STATUS_WIDTH = 10;

    private final UserDao userDao;

    public AdminMenu(Scanner scanner, UserDao userDao) {
        super(scanner);
        this.userDao = userDao;
    }

    @Override
    protected String title() {
        return "HOSPITAL ADMINISTRATOR MENU";
    }

    @Override
    public boolean show(User admin) {

        while (true) {

            System.out.println("\n==== " + title() + " ====");
            System.out.println("Logged in as: " + admin.fullName()
                    + " (" + admin.username() + ")");

            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.println("5. Exit application");
            System.out.println("6. Update User Role and Status");

            int choice = readIntInput("Choose an option: ");

            try {

                switch (choice) {

                    case 1 -> viewAllUsers();

                    case 2 -> viewPendingUsers();

                    case 3 -> approveUserAccount();

                    case 4 -> {
                        System.out.println(
                                "Logout successful. Returning to the main menu.");
                        return false;
                    }

                    case 5 -> {
                        return true;
                    }

                    case 6 -> updateUser(admin);

                    default -> System.out.println(
                            "Invalid choice. Please enter a number from 1 to 6.");
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    // View all registered users
    private void viewAllUsers() throws SQLException {

        System.out.println("\n---- ALL SYSTEM USERS ----");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users are registered yet.");
            return;
        }

        printUserTable(users);
    }

    // View pending registrations
    private void viewPendingUsers() throws SQLException {

        System.out.println("\n---- PENDING REGISTRATIONS ----");

        List<User> pending = userDao.getPendingUsers();

        if (pending.isEmpty()) {
            System.out.println(
                    "There are no accounts awaiting approval.");
            return;
        }

        printUserTable(pending);
    }

    // Approve pending user accounts
    private void approveUserAccount() throws SQLException {

        System.out.println("\n---- APPROVE USER ACCOUNT ----");

        List<User> pending = userDao.getPendingUsers();

        if (pending.isEmpty()) {
            System.out.println(
                    "There are no accounts awaiting approval.");
            return;
        }

        printUserTable(pending);

        while (true) {

            int targetId = readIntInput(
                    "Enter Target User ID to approve (0 to cancel): ");

            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = findById(pending, targetId);

            if (target == null) {
                System.out.println(
                        "User ID " + targetId
                                + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(targetId)) {

                System.out.println("Success! Account '"
                        + target.username()
                        + "' (User ID " + targetId
                        + ", " + target.role().displayName()
                        + ") has been APPROVED.");

            } else {
                System.out.println(
                        "Approval failed: User ID "
                                + targetId + " no longer exists.");
            }

            return;
        }
    }

    // Update another user's role and status
    private void updateUser(User admin) throws SQLException {

        System.out.println("\n---- UPDATE USER ROLE AND STATUS ----");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users are registered.");
            return;
        }

        // Display all users
        printUserTable(users);

        // Ask for target User ID
        int targetUserId = readIntInput(
                "Enter Target User ID (0 to cancel): ");

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User target = findById(users, targetUserId);

        if (target == null) {
            System.out.println("User ID not found. Update cancelled.");
            return;
        }

        // Prevent admin from modifying their own active account
        if (targetUserId == admin.userId()) {

            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted.");

            return;
        }

        System.out.println("\nSelected User: " + target.username());
        System.out.println("Current Role: " + target.role().name());
        System.out.println("Current Status: " + target.status());

        // Read new role
        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Read new status
        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Update database using UserDao
        boolean updated = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus
        );

        if (updated) {

            System.out.println("\n==================================");
            System.out.println("USER UPDATED SUCCESSFULLY!");
            System.out.println("Username: " + target.username());
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);
            System.out.println("==================================");

        } else {
            System.out.println(
                    "Update failed. User ID not found.");
        }
    }

    // Prevent crashes caused by invalid numeric input
    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input! Please enter numbers only.");
            }
        }
    }

    // Validate roles based on Role.java
    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable Roles:");

            for (Role role : Role.values()) {
                System.out.println("- " + role.name());
            }

            System.out.print("Enter New Role (0 to cancel): ");

            String input = scanner.nextLine()
                    .trim()
                    .toUpperCase();

            if (input.equals("0")) {
                return null;
            }

            try {

                return Role.valueOf(input).name();

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "Invalid role! Please choose from the available roles.");
            }
        }
    }

    // Validate account status
    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable Statuses:");
            System.out.println("- APPROVED");
            System.out.println("- PENDING");

            System.out.print("Enter New Status (0 to cancel): ");

            String input = scanner.nextLine()
                    .trim()
                    .toUpperCase();

            if (input.equals("0")) {
                return null;
            }

            if (input.equals("APPROVED")
                    || input.equals("PENDING")) {

                return input;
            }

            System.out.println(
                    "Invalid status! Enter APPROVED or PENDING.");
        }
    }

    // Find user by ID
    private User findById(List<User> users, int userId) {

        for (User user : users) {

            if (user.userId() == userId) {
                return user;
            }
        }

        return null;
    }

    // Print users in an aligned table
    private void printUserTable(List<User> users) {

        String rowFormat = "| %-"
                + ID_WIDTH + "s | %-"
                + USERNAME_WIDTH + "s | %-"
                + ROLE_WIDTH + "s | %-"
                + STATUS_WIDTH + "s |%n";

        String border = "+"
                + "-".repeat(ID_WIDTH + 2)
                + "+" + "-".repeat(USERNAME_WIDTH + 2)
                + "+" + "-".repeat(ROLE_WIDTH + 2)
                + "+" + "-".repeat(STATUS_WIDTH + 2)
                + "+";

        System.out.println(border);

        System.out.printf(
                rowFormat, "ID", "Username", "Role", "Status");

        System.out.println(border);

        for (User user : users) {

            System.out.printf(
                    rowFormat,
                    user.userId(),
                    truncate(user.username(), USERNAME_WIDTH),
                    truncate(user.role().displayName(), ROLE_WIDTH),
                    user.status()
            );
        }

        System.out.println(border);

        System.out.println(users.size() + " user(s) listed.");
    }

    // Shorten long text to fit the table
    private String truncate(String value, int maxLength) {

        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength - 3) + "...";
    }
}
