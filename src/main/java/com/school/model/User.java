
package com.school.model;

public record User(
        int userId,
        String username,
        String fullName,
        Role role,
        String status
) {

    // CONSTRUCTOR IS PROVIDED BY THE RECORD

    // GET USER ID
    public int getUserId() {
        return userId;
    }

    // GET USERNAME
    public String getUsername() {
        return username;
    }

    // GET FULL NAME
    public String getFullName() {
        return fullName;
    }

    // GET ROLE
    public Role getRole() {
        return role;
    }

    // GET STATUS
    public String getStatus() {
        return status;
    }

    // DISPLAY USER INFORMATION
    public void displayInfo() {
        System.out.println("================================");
        System.out.println("       USER INFORMATION");
        System.out.println("================================");
        System.out.println("User ID: " + userId);
        System.out.println("Username: " + username);
        System.out.println("Full Name: " + fullName);
        System.out.println("Role: " + role.displayName());
        System.out.println("Status: " + status);
        System.out.println("================================");
    }
}
