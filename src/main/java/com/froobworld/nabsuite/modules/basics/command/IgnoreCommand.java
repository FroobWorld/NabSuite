package com.froobworld.nabsuite.modules.basics.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.entity.Player;

import java.util.List;

public class IgnoreCommand extends NabParentCommand {

    public IgnoreCommand(BasicsModule basicsModule) {
        super(
                "ignore",
                "Manage your ignore list.",
                "nabsuite.command.ignore",
                Player.class
        );
        childCommands.addAll(List.of(
                new IgnoreAddCommand(basicsModule),
                new IgnoreRemoveCommand(basicsModule),
                new IgnoreListCommand(basicsModule)
        ));
    }

}
