package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SetSpawnCommand(BasicsModule basicsModule) {
        super(
                "setspawn",
                "Set the server's spawn to your location.",
                "nabsuite.command.setspawn",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        basicsModule.getSpawnManager().setSpawnLocation(player.getLocation());
        player.sendMessage(Component.text("Spawn set.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
