package com.froobworld.nabsuite.modules.protect.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.entity.Player;

import java.util.List;

public class HorseCommand extends NabParentCommand {

    public HorseCommand(ProtectModule protectModule) {
        super(
                "horse",
                "Manage a claimed horse.",
                "nabsuite.command.horse",
                Player.class
        );
        childCommands.addAll(List.of(
                new HorseInfoCommand(protectModule),
                new HorseAddUserCommand(protectModule),
                new HorseRemUserCommand(protectModule)
        ));
    }

}
