package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.horse.HorseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ClaimHorseCommand extends NabCommand {
    private final ProtectModule protectModule;

    public ClaimHorseCommand(ProtectModule protectModule) {
        super(
                "claimhorse",
                "Protect a horse from theft.",
                "nabsuite.command.claimhorse",
                Player.class,
                "protecthorse"
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractHorse)) {
            player.sendMessage(Component.text("You need to be on the horse you wish to protect.", NamedTextColor.RED));
            return;
        }
        HorseManager horseManager = protectModule.getHorseManager();
        if (horseManager.getHorse(vehicle.getUniqueId()) != null) {
            player.sendMessage(Component.text("This horse has already been claimed.", NamedTextColor.RED));
            return;
        }
        horseManager.protectHorse(vehicle.getUniqueId(), player.getUniqueId());
        player.sendMessage(Component.text("Horse claimed.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
