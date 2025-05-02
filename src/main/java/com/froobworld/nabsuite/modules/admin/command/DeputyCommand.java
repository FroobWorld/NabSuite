package com.froobworld.nabsuite.modules.admin.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DeputyCommand extends NabParentCommand {

    public DeputyCommand(AdminModule adminModule) {
        super(
                "deputy",
                "Manage deputies.",
                "nabsuite.command.deputy",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new DeputyAddCommand(adminModule),
                new DeputyRenewCommand(adminModule),
                new DeputyListCommand(adminModule),
                new DeputyRemoveCommand(adminModule)
        ));
    }
}
