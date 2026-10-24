package com.school.dao;

import com.school.model.Role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Creates every table from the approved ERD if it does not exist yet.
 * Tables are created parent-first so foreign keys always reference an existing table.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            rejectOutdatedUsersTable(connection);

            statement.execute(usersTable());
            statement.execute(patientsTable());
            statement.execute(appointmentsTable());
            statement.execute(medicalInvoicesTable());
            statement.execute(systemLogsTable());
        }
    }

    private static String usersTable() {
        // Allowed roles come straight from the Role enum so the CHECK never drifts from the code.
        String allowedRoles = Arrays.stream(Role.values())
                .map(role -> "'" + role.name() + "'")
                .collect(Collectors.joining(", "));

        return """
                CREATE TABLE IF NOT EXISTS users (
                    user_id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    username   TEXT NOT NULL UNIQUE,
                    password   TEXT NOT NULL,
                    role       TEXT NOT NULL CHECK (role IN (%s)),
                    full_name  TEXT NOT NULL,
                    status     TEXT NOT NULL DEFAULT 'PENDING'
                               CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(allowedRoles);
    }

    private static String patientsTable() {
        return """
                CREATE TABLE IF NOT EXISTS patients (
                    patient_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name       TEXT NOT NULL,
                    age             INTEGER,
                    gender          TEXT,
                    contact_number  TEXT,
                    medical_history TEXT
                )
                """;
    }

    private static String appointmentsTable() {
        return """
                CREATE TABLE IF NOT EXISTS appointments (
                    appointment_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id       INTEGER NOT NULL,
                    doctor_id        INTEGER NOT NULL,
                    appointment_date TIMESTAMP NOT NULL,
                    status           TEXT NOT NULL DEFAULT 'SCHEDULED',
                    FOREIGN KEY (patient_id) REFERENCES patients (patient_id),
                    FOREIGN KEY (doctor_id)  REFERENCES users (user_id)
                )
                """;
    }

    private static String medicalInvoicesTable() {
        // UNIQUE on appointment_id enforces the 1:1 Appointments-to-Medical_Invoices relationship.
        return """
                CREATE TABLE IF NOT EXISTS medical_invoices (
                    invoice_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    appointment_id   INTEGER NOT NULL UNIQUE,
                    consultation_fee REAL NOT NULL DEFAULT 0,
                    total_amount     REAL NOT NULL DEFAULT 0,
                    payment_status   TEXT NOT NULL DEFAULT 'UNPAID',
                    FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id)
                )
                """;
    }

    private static String systemLogsTable() {
        return """
                CREATE TABLE IF NOT EXISTS system_logs (
                    log_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id      INTEGER NOT NULL,
                    action_taken TEXT NOT NULL,
                    timestamp    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users (user_id)
                )
                """;
    }

    /**
     * Older versions of this project created users(id, roles, ...) for TEACHER/STUDENT.
     * CREATE TABLE IF NOT EXISTS would silently keep that table, so fail with a clear message instead.
     */
    private static void rejectOutdatedUsersTable(Connection connection) throws SQLException {
        boolean tableExists = false;
        boolean hasUserId = false;

        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(users)")) {
            while (columns.next()) {
                tableExists = true;
                if ("user_id".equalsIgnoreCase(columns.getString("name"))) {
                    hasUserId = true;
                }
            }
        }

        if (tableExists && !hasUserId) {
            throw new SQLException("users.db contains an outdated 'users' table from the previous version. "
                    + "Delete users.db and start the application again to create the new schema.");
        }
    }
}
