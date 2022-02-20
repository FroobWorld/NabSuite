package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.horse.Horse;
import com.froobworld.nabsuite.modules.protect.horse.HorseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UnclaimHorseCommand extends NabCommand {
    private final ProtectModule protectModule;

    public UnclaimHorseCommand(ProtectModule protectModule) {
        super(
                "unclaimhorse",
                "Unclaim a horse you own.",
                "nabsuite.command.unclaimhorse",
                Player.class,
                "unprotecthorse"
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractHorse)) {
            player.sendMessage(Component.text("You need to be on the horse you wish to unclaim.", NamedTextColor.RED));
            return;
        }
        HorseManager horseManager = protectModule.getHorseManager();
        Horse horse = horseManager.getHorse(vehicle.getUniqueId());
        if (horse == null) {
            player.sendMessage(Component.text("This horse hasn't been claimed.", NamedTextColor.RED));
            return;
        }
        horseManager.deleteHorse(horse);
        player.sendMessage(Component.text("Horse unclaimed.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
