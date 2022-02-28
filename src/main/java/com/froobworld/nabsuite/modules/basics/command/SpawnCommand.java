package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SpawnCommand(BasicsModule basicsModule) {
        super(
                "spawn",
                "Teleport to spawn.",
                "nabsuite.command.spawn",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        basicsModule.getPlayerTeleporter().teleport(player, basicsModule.getSpawnManager().getSpawnLocation());
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
