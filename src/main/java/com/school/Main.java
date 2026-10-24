package com.school;

import com.school.dao.DatabaseInitializer;
import com.school.dao.UserDao;
import com.school.ui.Authentication;

import java.util.Scanner;

/**
 * Startup entry point only: initialize the database, create the shared
 * DAO and Scanner, then hand control to Authentication.
 */
public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Initializing database...");
            DatabaseInitializer.initialize();
            System.out.println("Database ready.");

            UserDao userDao = new UserDao();

            new Authentication(userDao, scanner).start();
        } catch (Exception e) {
            System.err.println("Application could not start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
