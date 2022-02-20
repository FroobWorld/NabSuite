package com.froobworld.nabsuite.modules.basics.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.entity.Player;

import java.util.List;

public class FriendCommand extends NabParentCommand {

    public FriendCommand(BasicsModule basicsModule) {
        super(
                "friend",
                "Manage your friends list.",
                "nabsuite.command.friend",
                Player.class
        );
        childCommands.addAll(List.of(
                new FriendAddCommand(basicsModule),
                new FriendRemoveCommand(basicsModule),
                new FriendListCommand(basicsModule)
        ));
    }

}
