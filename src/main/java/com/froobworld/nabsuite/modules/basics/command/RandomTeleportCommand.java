package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.random.RandomTeleportManager;
import com.froobworld.nabsuite.util.DurationDisplayer;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomTeleportCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public RandomTeleportCommand(BasicsModule basicsModule) {
        super(
                "rtp",
                "Teleport to a random location.",
                "nabsuite.command.rtp",
                Player.class,
                "randomteleport", "randomtp", "wild"
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (!basicsModule.getConfig().randomTeleport.enabledWorlds.get().contains(player.getWorld().getName())) {
            player.sendMessage(Component.text("Random teleporting is not supported in this world.", NamedTextColor.RED));
            return;
        }

        RandomTeleportManager randomTeleportManager = basicsModule.getRandomTeleportManager();
        if (randomTeleportManager.isTeleportInProgress(player)) {
            player.sendMessage(Component.text("We're still working on your last random teleport request.", NamedTextColor.RED));
            return;
        }

        if (randomTeleportManager.getRandomTeleportAllowance(player) <= 0) {
            player.sendMessage(
                    Component.text("You don't have any random teleports left. You will gain another in ", NamedTextColor.RED)
                            .append(Component.text(DurationDisplayer.asDurationString(randomTeleportManager.getTimeUntilNextRandomTeleport(player)) + ".", NamedTextColor.RED))
            );
            return;
        }

        player.sendMessage(Component.text("Attempting to find a location...", NamedTextColor.YELLOW));
        randomTeleportManager.randomTeleport(player)
                .thenAccept(location -> {
                    if (location == null) {
                        player.sendMessage(Component.text("We couldn't find a suitable location. Try again.", NamedTextColor.RED));
                    } else {
                        int remainingTeleports = randomTeleportManager.getRandomTeleportAllowance(player);
                        if (remainingTeleports <= 0) {
                            player.sendMessage(
                                    Component.text("You have used your last random teleport. You will gain another in ", NamedTextColor.YELLOW)
                                            .append(Component.text(DurationDisplayer.asDurationString(randomTeleportManager.getTimeUntilNextRandomTeleport(player)) + ".", NamedTextColor.YELLOW))
                            );
                        } else {
                            player.sendMessage(Component.text("You have " + NumberDisplayer.toStringWithModifier(remainingTeleports, " random teleport", " random teleports", false) + " remaining.", NamedTextColor.YELLOW));
                        }
                    }
                });
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
