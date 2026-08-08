package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MsptContribsCommand extends NabCommand {
    private final MechsModule mechsModule;

    public MsptContribsCommand(MechsModule mechsModule) {
        super(
                "msptcontribs",
                "Get estimated MSPT contributions of players.",
                "nabsuite.command.msptcontribs",
                CommandSender.class,
                "mspthogs",
                "lagblame"
        );
        this.mechsModule = mechsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        if (!mechsModule.getPerformanceMonitor().enabled()) {
            context.getSender().sendMessage(Component.text("Performance monitor is not enabled.", NamedTextColor.RED));
            return;
        }

        Map<Player, Double> contribs = mechsModule.getPerformanceMonitor().getEstimatedMsptContributions();
        if (contribs == null) {
            context.getSender().sendMessage(Component.text("Performance monitor needs more data - please wait.", NamedTextColor.RED));
            return;
        }

        if (contribs.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no players online.", NamedTextColor.YELLOW));
            return;
        }

        // sort highest to lowest
        List<Map.Entry<Player, Double>> entries = new ArrayList<>(contribs.entrySet());
        entries.sort(Comparator.comparingDouble(entry -> -entry.getValue()));

        context.getSender().sendMessage(Component.text("Estimated MSPT contributions:", NamedTextColor.YELLOW));
        for (Map.Entry<Player, Double> entry : entries) {
            context.getSender().sendMessage(
                    Component.text(String.format("%6.2f..  ", entry.getValue()))
                            .append(entry.getKey().displayName())
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
