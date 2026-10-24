package com.school.ui;

import java.util.Scanner;

public class DoctorMenu extends RoleMenu {
    public DoctorMenu(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected String title() {
        return "DOCTOR MENU";
    }
}
