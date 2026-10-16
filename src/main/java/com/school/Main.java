package com.school;

import com.school.dao.DatabaseConnection;
import com.school.dao.UserDao;
import com.school.ui.Menu;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.initializeDatabase();
            UserDao userDao = new UserDao();
            new Menu(userDao).start();
        } catch (Exception e) {
            System.err.println("Application could not start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
