package com.froobworld.nabsuite.modules.protect.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AreaCommand extends NabParentCommand {

    public AreaCommand(ProtectModule protectModule) {
        super(
                "area",
                "Manage a claimed area.",
                "nabsuite.command.area",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new AreaHereCommand(protectModule),
                new AreaInfoCommand(protectModule),
                new AreaAddUserCommand(protectModule),
                new AreaAddManagerCommand(protectModule),
                new AreaAddOwnerCommand(protectModule),
                new AreaRemUserCommand(protectModule),
                new AreaRemManagerCommand(protectModule),
                new AreaRemOwnerCommand(protectModule),
                new AreaAddFlagCommand(protectModule),
                new AreaRemFlagCommand(protectModule),
                new AreaSetVerticalBoundsCommand(protectModule),
                new AreaApproveCommand(protectModule),
                new AreaDenyCommand(protectModule),
                new AreaReviewCommand(protectModule),
                new AreaTeleportCommand(protectModule)
        ));
    }

}
