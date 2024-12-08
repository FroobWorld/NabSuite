package com.froobworld.nabsuite.modules.protect.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GlobalAreaCommand extends NabParentCommand {

    public GlobalAreaCommand(ProtectModule protectModule) {
        super(
                "globalarea",
                "Manage a world's global area.",
                "nabsuite.command.globalarea",
                CommandSender.class,
                "garea"
        );
        childCommands.addAll(List.of(
                new GlobalAreaAddUserCommand(protectModule),
                new GlobalAreaRemUserCommand(protectModule),
                new GlobalAreaAddFlagCommand(protectModule),
                new GlobalAreaRemFlagCommand(protectModule),
                new GlobalAreaInfoCommand(protectModule),
                new GlobalAreaSubAreaCommand(protectModule)
        ));
    }

}
