package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.horse.Horse;
import com.froobworld.nabsuite.modules.protect.horse.HorseManager;
import com.froobworld.nabsuite.modules.protect.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class HorseInfoCommand extends NabCommand {
    private final ProtectModule protectModule;

    public HorseInfoCommand(ProtectModule protectModule) {
        super(
                "info",
                "Get information about a claimed horse.",
                "nabsuite.command.horse.info",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractHorse)) {
            player.sendMessage(Component.text("You need to be on a horse to use this command.", NamedTextColor.RED));
            return;
        }
        HorseManager horseManager = protectModule.getHorseManager();
        Horse horse = horseManager.getHorse(vehicle.getUniqueId());
        if (horse == null) {
            player.sendMessage(Component.text("This horse has not been claimed.", NamedTextColor.YELLOW));
            return;
        }

        context.getSender().sendMessage(
                Component.text("----- Information for claimed horse -----").color(NamedTextColor.YELLOW)
        );
        context.getSender().sendMessage(
                Component.text("UUID: ", NamedTextColor.YELLOW)
                        .append(Component.text(vehicle.getUniqueId().toString(), NamedTextColor.WHITE))

        );
        PlayerIdentity owner = protectModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(horse.getOwner());
        context.getSender().sendMessage(
                Component.text("Owner: ", NamedTextColor.YELLOW)
                        .append(owner.displayName().color(NamedTextColor.WHITE))

        );
        context.getSender().sendMessage(
                Component.text("Users: ", NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        horse.getUsers().stream()
                                                .map(User::asDecoratedComponent)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )

        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/horse info";
    }
}
