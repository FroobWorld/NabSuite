package com.froobworld.nabsuite.modules.basics.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ChannelCommand extends NabParentCommand {

    public ChannelCommand(BasicsModule basicsModule) {
        super(
                "channel",
                "Manage a chat channel.",
                "nabsuite.command.channel",
                CommandSender.class,
                "ch"
        );
        childCommands.addAll(List.of(
                new ChannelCreateCommand(basicsModule),
                new ChannelDeleteCommand(basicsModule),
                new ChannelInfoCommand(basicsModule),
                new ChannelPlayerlistCommand(basicsModule),
                new ChannelAddUserCommand(basicsModule),
                new ChannelAddManagerCommand(basicsModule),
                new ChannelAddOwnerCommand(basicsModule),
                new ChannelRemUserCommand(basicsModule),
                new ChannelRemManagerCommand(basicsModule),
                new ChannelRemOwnerCommand(basicsModule)
        ));
    }
}
