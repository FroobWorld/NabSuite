package com.froobworld.nabsuite.modules.nabmode.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import com.froobworld.nabsuite.modules.nabmode.nabdimension.NabDimensionManager;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NabsBeGoneCommand extends NabCommand {

    private final NabModeModule nabModeModule;
    private final BasicsModule basicsModule;
    private final NabDimensionManager nabDimensionManager;

    public NabsBeGoneCommand(NabModeModule nabModeModule) {
        super(
                "nabsbegone",
                "Remove player(s) from nabworld.",
                "nabsuite.command.nabsbegone",
                CommandSender.class
        );
        this.nabModeModule = nabModeModule;
        this.basicsModule = nabModeModule.getPlugin().getModule(BasicsModule.class);
        this.nabDimensionManager = nabModeModule.getNabModeManager().getNabDimensionManager();
    }

    private void sendAway(Player player) {
        Location backLocation = basicsModule.getBackManager().getBackLocation(player);
        if (backLocation == null || nabDimensionManager.getNabWorld().equals(backLocation.getWorld())) {
            backLocation = basicsModule.getSpawnManager().getSpawnLocation();
        }
        basicsModule.getPlayerTeleporter().teleportAsync(player, backLocation).thenAccept(location -> {
            Location newBackLocation = basicsModule.getBackManager().getBackLocation(player);
            if (newBackLocation != null && nabDimensionManager.getNabWorld().equals(newBackLocation.getWorld())) {
                basicsModule.getBackManager().setBackLocation(player, null);
            }
            player.sendMessage(
                    Component.text("Teleported to your previous location.").color(NamedTextColor.YELLOW)
            );
        });
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<Component> nabNames = new LinkedList<>();

        Optional<Player> playerArgument = context.getOptional("player");
        if (playerArgument.isPresent()) {
            Player player = playerArgument.get();
            sendAway(player);
            nabNames.add(player.displayName());
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!nabDimensionManager.getNabWorld().equals(player.getWorld())) {
                    continue;
                }
                if (player.equals(context.getSender()) || nabModeModule.getNabModeManager().isNabMode(player)) {
                    continue;
                }
                sendAway(player);
                nabNames.add(player.displayName());
            }
        }

        if (nabNames.isEmpty()) {
            context.getSender().sendMessage(Component.text("The nabs are already gone.").color(NamedTextColor.YELLOW));
        } else {
            context.getSender().sendMessage(
                    Component.text(NumberDisplayer.toStringWithModifier(nabNames.size(), " nab has", " nabs have", true) + " been removed: ").color(NamedTextColor.YELLOW)
                            .append(Component.join(
                                    JoinConfiguration.separator(Component.text(", ").color(NamedTextColor.YELLOW)),
                                    nabNames
                            ))
                            .append(Component.text("."))
            );
        }

    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerArgument<>(
                        false,
                        "player",
                        new ArgumentPredicate<>(
                                true,
                                (context, player) -> nabDimensionManager.getNabWorld().equals(player.getWorld()),
                                "That player isn't in nabworld."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/nabsbegone [player]";
    }
}



