package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.command.NabCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public BackCommand(BasicsModule basicsModule) {
        super(
                "back",
                "Teleport to your previous location.",
                "nabsuite.command.back",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Location backLocation = basicsModule.getBackManager().getBackLocation(player);
        if (backLocation == null) {
            player.sendMessage(
                    Component.text("There is nowhere to go back to.").color(NamedTextColor.RED)
            );
            return;
        }
        basicsModule.getPlayerTeleporter().teleportAsync(player, backLocation).thenAccept(location -> {
            player.sendMessage(
                    Component.text("Teleported to your previous location.").color(NamedTextColor.YELLOW)
            );
        });
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
