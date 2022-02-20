package com.froobworld.nabsuite.modules.admin.tasks;

import net.kyori.adventure.text.Component;

public class StaffTask {
    private final String permission;
    private final Component taskMessage;

    public StaffTask(String permission, Component taskMessage) {
        this.permission = permission;
        this.taskMessage = taskMessage;
    }

    public final String getPermission() {
        return permission;
    }

    public final Component getTaskMessage() {
        return taskMessage;
    }

}
