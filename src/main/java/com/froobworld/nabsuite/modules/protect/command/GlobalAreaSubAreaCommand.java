package com.froobworld.nabsuite.modules.protect.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GlobalAreaSubAreaCommand extends NabParentCommand {

    public GlobalAreaSubAreaCommand(ProtectModule protectModule) {
        super(
                "subarea",
                "Manage the subareas of a global area.",
                "nabsuite.command.globalarea.subarea",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new GlobalAreaSubAreaCreateCommand(protectModule),
                new GlobalAreaSubAreaDeleteCommand(protectModule),
                new GlobalAreaSubAreaRedefineCommand(protectModule),
                new GlobalAreaSubAreaAddFlagCommand(protectModule),
                new GlobalAreaSubAreaRemFlagCommand(protectModule),
                new GlobalAreaSubAreaInfoCommand(protectModule)
        ));
    }

    @Override
    public String getUsage() {
        return super.getUsage();
    }
}
