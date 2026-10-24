package com.school.model;

public record User(int userId, String username, String password, String fullName, Role role, String status) {
}
