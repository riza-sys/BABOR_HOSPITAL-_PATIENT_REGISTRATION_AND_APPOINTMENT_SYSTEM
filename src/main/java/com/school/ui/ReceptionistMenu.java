package com.school.ui;

import java.util.Scanner;

public class ReceptionistMenu extends RoleMenu {
    public ReceptionistMenu(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected String title() {
        return "CLINIC RECEPTIONIST MENU";
    }
}
