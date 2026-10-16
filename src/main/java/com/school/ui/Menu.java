package com.school.ui;

import com.school.dao.UserDao;
import com.school.model.User;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Menu {
    private final UserDao userDao;
    private final Scanner scanner = new Scanner(System.in);

    public Menu(UserDao userDao) {
        this.userDao = userDao;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n==== ACCOUNT SYSTEM ====");
            System.out.println("1. Register account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    running = false;
                    System.out.println("Application closed. Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n==== REGISTER ====");
        String username = readNonBlank("Enter username: ");
        String password = readNonBlank("Enter password: ");

        System.out.println("Select role:");
        System.out.println("1. TEACHER");
        System.out.println("2. STUDENT");
        System.out.print("Choose role: ");
        String roleChoice = scanner.nextLine().trim();

        String role;
        if (roleChoice.equals("1")) {
            role = "TEACHER";
        } else if (roleChoice.equals("2")) {
            role = "STUDENT";
        } else {
            System.out.println("Invalid role selection. Registration cancelled.");
            return;
        }

        try {
            if (userDao.register(username, password, role)) {
                System.out.println("Account registered successfully.");
                System.out.println("Role: " + role);
                System.out.println("Status: PENDING");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println("That username is already taken. Please register with another username.");
            } else {
                System.out.println("Database error while registering: " + e.getMessage());
            }
        } catch (GeneralSecurityException e) {
            System.out.println("Could not secure the password: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\n==== LOGIN ====");
        String username = readNonBlank("Enter username: ");
        String password = readNonBlank("Enter password: ");

        try {
            Optional<User> result = userDao.login(username, password);
            if (result.isEmpty()) {
                System.out.println("Invalid username or password.");
                return;
            }

            User user = result.get();
            System.out.println("Login successful. Welcome, " + user.username() + "!");
            System.out.println("Role: " + user.roles());
            System.out.println("Account status: " + user.status());
            sessionMenu(user);
        } catch (SQLException e) {
            System.out.println("Database error while logging in: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.out.println("Could not verify the password: " + e.getMessage());
        }
    }

    private void sessionMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==== USER SESSION ====");
            System.out.println("Logged in as: " + user.username());
            System.out.println("1. Logout");
            System.out.println("2. Exit application");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    loggedIn = false;
                    System.out.println("Logout successful. Returning to the main menu.");
                }
                case "2" -> {
                    System.out.println("Application closed. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }
}
