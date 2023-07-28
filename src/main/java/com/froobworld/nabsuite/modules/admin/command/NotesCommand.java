package com.froobworld.nabsuite.modules.admin.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class NotesCommand extends NabParentCommand {

    public NotesCommand(AdminModule adminModule) {
        super(
                "notes",
                "Manage player notes.",
                "nabsuite.command.notes",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new NotesAddCommand(adminModule),
                new NotesReadCommand(adminModule)
        ));
    }
}
