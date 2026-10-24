package com.school.model;

/**
 * The three user roles from the approved System Proposal / ERD
 * (Users.role: Hospital Administrator / Clinic Receptionist / Doctor).
 * The enum name is what gets stored in the users.role column.
 */
public enum Role {
    HOSPITAL_ADMINISTRATOR("Hospital Administrator"),
    CLINIC_RECEPTIONIST("Clinic Receptionist"),
    DOCTOR("Doctor");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
