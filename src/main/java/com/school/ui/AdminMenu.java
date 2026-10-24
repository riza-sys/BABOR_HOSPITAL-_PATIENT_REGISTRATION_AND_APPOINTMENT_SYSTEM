package com.school.ui;

import com.school.dao.UserDao;
import com.school.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Menu for the Hospital Administrator: view all users, view pending
 * registrations, and approve pending accounts.
 */
public class AdminMenu extends RoleMenu {
    private static final int ID_WIDTH = 5;
    private static final int USERNAME_WIDTH = 15;
    private static final int ROLE_WIDTH = 22;   // "Hospital Administrator" is the longest role name
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

    /**
     * @return true if the administrator chose to exit the whole application
     */
    @Override
    public boolean show(User admin) {
        while (true) {
            System.out.println("\n==== " + title() + " ====");
            System.out.println("Logged in as: " + admin.fullName() + " (" + admin.username() + ")");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.println("5. Exit application");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> viewAllUsers();
                    case "2" -> viewPendingUsers();
                    case "3" -> approveUserAccount();
                    case "4" -> {
                        System.out.println("Logout successful. Returning to the main menu.");
                        return false;
                    }
                    case "5" -> {
                        return true;
                    }
                    default -> System.out.println("Invalid choice. Please enter a number from 1 to 5.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void viewAllUsers() throws SQLException {
        System.out.println("\n---- ALL SYSTEM USERS ----");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users are registered yet.");
            return;
        }
        printUserTable(users);
    }

    private void viewPendingUsers() throws SQLException {
        System.out.println("\n---- PENDING REGISTRATIONS ----");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("There are no accounts awaiting approval.");
            return;
        }
        printUserTable(pending);
    }

    private void approveUserAccount() throws SQLException {
        System.out.println("\n---- APPROVE USER ACCOUNT ----");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("There are no accounts awaiting approval.");
            return;
        }
        printUserTable(pending);

        while (true) {
            System.out.print("Enter Target User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();

            int targetId;
            try {
                targetId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric User ID.");
                continue;
            }

            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = findById(pending, targetId);
            if (target == null) {
                System.out.println("User ID " + targetId + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(targetId)) {
                System.out.println("Success! Account '" + target.username() + "' (User ID " + targetId
                        + ", " + target.role().displayName() + ") has been APPROVED.");
            } else {
                System.out.println("Approval failed: User ID " + targetId + " no longer exists.");
            }
            return;
        }
    }

    private User findById(List<User> users, int userId) {
        for (User user : users) {
            if (user.userId() == userId) {
                return user;
            }
        }
        return null;
    }

    /**
     * Prints any list of users as an aligned table with a border and header row.
     */
    private void printUserTable(List<User> users) {
        String rowFormat = "| %-" + ID_WIDTH + "s | %-" + USERNAME_WIDTH + "s | %-"
                + ROLE_WIDTH + "s | %-" + STATUS_WIDTH + "s |%n";
        String border = "+" + "-".repeat(ID_WIDTH + 2)
                + "+" + "-".repeat(USERNAME_WIDTH + 2)
                + "+" + "-".repeat(ROLE_WIDTH + 2)
                + "+" + "-".repeat(STATUS_WIDTH + 2) + "+";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User user : users) {
            System.out.printf(rowFormat,
                    user.userId(),
                    truncate(user.username(), USERNAME_WIDTH),
                    user.role().displayName(),
                    user.status());
        }
        System.out.println(border);
        System.out.println(users.size() + " user(s) listed.");
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }
}
