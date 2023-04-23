package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EffectiveViewDistanceCommand extends NabCommand {

    public EffectiveViewDistanceCommand() {
        super(
                "effectivevd",
                "Get the effective view distance of the server.",
                "nabsuite.command.effectivevd",
                CommandSender.class,
                "evd"
        );
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        int totalChunks = 0;
        int playerCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            totalChunks += Math.pow((player.getViewDistance() * 2 + 1), 2);
            playerCount++;
        }
        double effectiveVd = 0;
        if (playerCount > 0) {
            effectiveVd = (Math.sqrt((double) totalChunks / (double) playerCount) - 1.0) / 2.0;
        }

        context.getSender().sendMessage(
                Component.text("The effective average view distance is ")
                        .append(Component.text(String.format("%.1f", effectiveVd), NamedTextColor.RED))
                        .append(Component.text("."))
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
