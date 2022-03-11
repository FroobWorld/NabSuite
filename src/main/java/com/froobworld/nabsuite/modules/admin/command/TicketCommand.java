package com.froobworld.nabsuite.modules.admin.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TicketCommand extends NabParentCommand {

    public TicketCommand(AdminModule adminModule) {
        super(
                "ticket",
                "Manage player created tickets.",
                "nabsuite.command.ticket",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new TicketReadCommand(adminModule),
                new TicketTeleportCommand(adminModule),
                new TicketAddNoteCommand(adminModule),
                new TicketCloseCommand(adminModule)
        ));
    }

}
