package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ToggleViewDistanceCommand extends NabCommand {
    private final MechsModule mechsModule;

    public ToggleViewDistanceCommand(MechsModule mechsModule) {
        super(
                "togglevd",
                "Toggle your view distance cap.",
                "nabsuite.command.togglevd",
                Player.class
        );
        this.mechsModule = mechsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        boolean confirmed = context.getOptional("confirmation").isPresent();
        if (mechsModule.getViewDistanceManager().isViewDistanceCapped(player) && !confirmed) {
            player.sendMessage(Component.empty());
            player.sendMessage(
                    Component.text("Uncapping your view distance may result in connection problems.", NamedTextColor.RED)
            );
            player.sendMessage(Component.empty());
            player.sendMessage(
                    Component.text("If you are sure you want to uncap your view distance, please type ")
                            .append(Component.text("/togglevd confirm", NamedTextColor.YELLOW))
                            .append(Component.text("."))
                            .color(NamedTextColor.RED)
            );
            player.sendMessage(Component.empty());
            return;
        }
        mechsModule.getViewDistanceManager().setViewDistanceCapped(player, !mechsModule.getViewDistanceManager().isViewDistanceCapped(player));
        if (mechsModule.getViewDistanceManager().isViewDistanceCapped(player)) {
            player.sendMessage(
                    Component.text("Your view distance has been capped.", NamedTextColor.YELLOW)
            );
        } else {
            player.sendMessage(
                    Component.text("Your view distance has been uncapped. If you notice latency issues or are being disconnected from the server, lower your render distance in your client options.", NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new StringArgument<>(
                                false,
                                "confirmation",
                                false,
                                List.of("confirm"),
                                new ArgumentPredicate<>(
                                        true,
                                        (context, string) -> string.equalsIgnoreCase("confirm"),
                                        "Confirmation argument must be 'confirm'"
                                )
                        )
                );
    }
}
