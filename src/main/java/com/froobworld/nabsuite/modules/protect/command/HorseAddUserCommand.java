package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.command.argument.UserArgument;
import com.froobworld.nabsuite.modules.protect.horse.Horse;
import com.froobworld.nabsuite.modules.protect.horse.HorseManager;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HorseAddUserCommand extends NabCommand {
    private final ProtectModule protectModule;

    public HorseAddUserCommand(ProtectModule protectModule) {
        super(
                "adduser",
                "Add a user to a horse.",
                "nabsuite.command.horse.adduser",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Horse horse = context.get("horse");
        User user = context.get("user");
        horse.addUser(user);
        context.getSender().sendMessage(
                Component.text("User added to horse.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder.argument(
                new UserArgument<>(
                        true,
                        "user",
                        protectModule.getPlugin(),
                        true,
                        new ArgumentPredicate<>(
                                false,
                                (context, user) -> {
                                    Player player = (Player) context.getSender();
                                    Entity vehicle = player.getVehicle();
                                    context.set("vehicle", vehicle);
                                    if (!(vehicle instanceof AbstractHorse)) {
                                        return false;
                                    }
                                    return true;
                                },
                                "You need to be on a horse to use this command."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                (context, user) -> {
                                    Entity vehicle = context.get("vehicle");
                                    HorseManager horseManager = protectModule.getHorseManager();
                                    Horse horse = horseManager.getHorse(vehicle.getUniqueId());
                                    if (horse == null) {
                                        return false;
                                    }
                                    context.set("horse", horse);
                                    return true;
                                },
                                "This horse has not been claimed."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                (context, user) -> {
                                    Player player = (Player) context.getSender();
                                    Horse horse = context.get("horse");
                                    return horse.isOwner(player) || player.hasPermission(HorseManager.EDIT_ALL_HORSES_PERMISSION);
                                },
                                "You don't have permission to manage this horse."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                (context, user) -> {
                                    Horse horse = context.get("horse");
                                    return !horse.isUser(user);
                                },
                                "That user is already a user of this horse."
                        )
                )
        );
    }

    @Override
    public String getUsage() {
        return "/horse adduser <user>";
    }

}
