package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AreaStopVisualisingCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaStopVisualisingCommand(ProtectModule protectModule) {
        super(
                "stopvisualising",
                "Stop visualising an area.",
                "nabsuite.command.area.visualise",
                Player.class,
                "stopvisualizing"
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (protectModule.getAreaManager().getAreaVisualiser().getVisualisedArea(player) == null) {
            player.sendMessage(
                    Component.text("You are not visualising any areas.", NamedTextColor.RED)
            );
            return;
        }
        protectModule.getAreaManager().getAreaVisualiser().stopVisualisation(player);
        player.sendMessage(
                Component.text("Visualisation has been stopped.", NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/area stopvisualising";
    }
}
