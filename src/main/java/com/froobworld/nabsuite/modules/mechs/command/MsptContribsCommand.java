package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.performance.PerformanceMonitor;
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

        PerformanceMonitor.ContributionEstimateResult result = mechsModule.getPerformanceMonitor().getEstimatedMsptContributions();
        if (result == null) {
            context.getSender().sendMessage(Component.text("Performance monitor needs more data - please wait.", NamedTextColor.RED));
            return;
        }

        if (result.contribs().isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no players online.", NamedTextColor.YELLOW));
            return;
        }

        // sort highest to lowest
        List<Map.Entry<Player, Double>> entries = new ArrayList<>(result.contribs().entrySet());
        entries.sort(Comparator.comparingDouble(entry -> -entry.getValue()));

        if (context.flags().hasFlag("stats")) {
            context.getSender().sendMessage(Component.text("Regression statistics:", NamedTextColor.YELLOW));
            context.getSender().sendMessage(String.format("c: %.5f", result.c()));
            context.getSender().sendMessage(String.format("e: %.5f", result.e()));
            context.getSender().sendMessage(String.format("t: %.5f", result.t()));
            context.getSender().sendMessage(String.format("Adj. R^2: %.5f", result.adjRsq()));
        } else {
            context.getSender().sendMessage(Component.text("Estimated MSPT contributions:", NamedTextColor.YELLOW));
            for (Map.Entry<Player, Double> entry : entries) {
                context.getSender().sendMessage(
                        Component.text(String.format("%6.2f  ", entry.getValue()))
                                .append(entry.getKey().displayName())
                );
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .flag(
                        CommandFlag
                                .newBuilder("stats")
                                .withAliases("s")
                                .build()
                );
    }
}
