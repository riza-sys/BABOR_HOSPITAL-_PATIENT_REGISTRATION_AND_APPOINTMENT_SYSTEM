package com.school.ui;

import com.school.dao.UserDao;
import com.school.model.Role;
import com.school.model.User;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Handles the first screen of the application: register, login, and routing
 * a logged-in user to the menu for their role. Contains no SQL; all data
 * access goes through the shared UserDao.
 */
public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    private final AdminMenu adminMenu;
    private final ReceptionistMenu receptionistMenu;
    private final DoctorMenu doctorMenu;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.adminMenu = new AdminMenu(scanner, userDao);
        this.receptionistMenu = new ReceptionistMenu(scanner);
        this.doctorMenu = new DoctorMenu(scanner);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n==== BABOR HOSPITAL - PATIENT REGISTRATION AND APPOINTMENT SYSTEM ====");
            System.out.println("1. Register account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> register();
                case "2" -> {
                    if (login()) {
                        running = false;
                    }
                }
                case "3" -> running = false;
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
        System.out.println("Application closed. Goodbye!");
    }

    private void register() {
        System.out.println("\n==== REGISTER ====");
        String fullName = readNonBlank("Enter full name: ");
        String username = readNonBlank("Enter username: ");
        String password = readNonBlank("Enter password: ");

        Role role = selectRole();
        if (role == null) {
            System.out.println("Invalid role selection. Registration cancelled.");
            return;
        }

        try {
            if (userDao.register(username, password, fullName, role)) {
                System.out.println("Account registered successfully.");
                System.out.println("Role: " + role.displayName());
                System.out.println("Status: PENDING");
            } else {
                System.out.println("That username is already taken. Please register with another username.");
            }
        } catch (SQLException e) {
            System.out.println("Database error while registering: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.out.println("Could not secure the password: " + e.getMessage());
        }
    }

    private Role selectRole() {
        Role[] roles = Role.values();
        System.out.println("Select role:");
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i].displayName());
        }
        System.out.print("Choose role: ");
        String input = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(input) - 1;
            return index >= 0 && index < roles.length ? roles[index] : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @return true if the user asked to exit the whole application from their menu
     */
    private boolean login() {
        System.out.println("\n==== LOGIN ====");
        String username = readNonBlank("Enter username: ");
        String password = readNonBlank("Enter password: ");

        try {
            Optional<User> result = userDao.login(username, password);
            if (result.isEmpty()) {
                System.out.println("Invalid username or password.");
                return false;
            }

            User user = result.get();
            System.out.println("Login successful. Welcome, " + user.fullName() + "!");
            System.out.println("Role: " + user.role().displayName());
            System.out.println("Account status: " + user.status());
            return routeToRoleMenu(user);
        } catch (SQLException e) {
            System.out.println("Database error while logging in: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.out.println("Could not verify the password: " + e.getMessage());
        }
        return false;
    }

    private boolean routeToRoleMenu(User user) {
        return switch (user.role()) {
            case HOSPITAL_ADMINISTRATOR -> adminMenu.show(user);
            case CLINIC_RECEPTIONIST -> receptionistMenu.show(user);
            case DOCTOR -> doctorMenu.show(user);
        };
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
