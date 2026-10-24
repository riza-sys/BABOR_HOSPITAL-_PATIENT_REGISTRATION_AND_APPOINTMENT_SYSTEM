package com.school.ui;

import com.school.model.User;

import java.util.Scanner;

/**
 * Base class for the role-based menus. Each role gets its own subclass so its
 * features can be added later without touching Authentication.
 */
public abstract class RoleMenu {
    protected final Scanner scanner;

    protected RoleMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    protected abstract String title();

    /**
     * Runs the menu until the user logs out or exits.
     *
     * @return true if the user chose to exit the whole application
     */
    public boolean show(User user) {
        while (true) {
            System.out.println("\n==== " + title() + " ====");
            System.out.println("Logged in as: " + user.fullName() + " (" + user.username() + ")");
            System.out.println("1. Logout");
            System.out.println("2. Exit application");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("Logout successful. Returning to the main menu.");
                    return false;
                }
                case "2" -> {
                    return true;
                }
                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
}
