package com.froobworld.nabsuite.modules.admin.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AntixrayCommand extends NabParentCommand {

    public AntixrayCommand(AdminModule adminModule) {
        super(
                "antixray",
                "Manage anti-xray on players.",
                "nabsuite.command.antixray",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new AntixrayEnableCommand(adminModule),
                new AntixrayDisableCommand(adminModule),
                new AntixrayCheckCommand(adminModule)
        ));
    }

}
