package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.vanish.VanishManager;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand extends NabCommand {
    private static final String TELEPORT_ALL_PERM = "nabsuite.teleport.all";
    private final BasicsModule basicsModule;

    public TeleportCommand(BasicsModule basicsModule) {
        super(
                "teleport",
                "Teleport to another player.",
                "nabsuite.command.teleport",
                Player.class,
                "tp", "tport", "port", "tele"
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Player subject = context.get("player");
        basicsModule.getPlayerTeleporter().teleport(sender, subject);
        sender.sendMessage(
                Component.text("Teleported to ")
                        .append(subject.displayName())
                        .append(Component.text("."))
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerArgument<>(
                        true,
                        "player",
                                new ArgumentPredicate<>(
                                        false,
                                        (context, subject) -> {
                                            Player sender = (Player) context.getSender();
                                            if (sender.hasPermission(TELEPORT_ALL_PERM)) {
                                                return true;
                                            }
                                            return basicsModule.getPlayerDataManager().getFriendManager().areFriends(sender, subject);
                                            },
                                        "Player is not a friend"
                                ),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, player) -> {
                                            if (context.getSender().hasPermission(TELEPORT_ALL_PERM)) {
                                                return true;
                                            }
                                            return basicsModule.getPlayerDataManager().getPlayerData(player).teleportRequestsEnabled();
                                        },
                                        "Player has teleportation disabled"
                                ),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, player) -> {
                                            Player sender = (Player) context.getSender();
                                            if (!sender.hasPermission(VanishManager.VANISH_SEE_PERMISSION)) {
                                                return true;
                                            }
                                            AdminModule adminModule = basicsModule.getPlugin().getModule(AdminModule.class);
                                            if (adminModule != null) {
                                                if (adminModule.getVanishManager().isVanished(player)) {
                                                    return adminModule.getVanishManager().isVanished(sender);
                                                }
                                            }
                                            return true;
                                        },
                                        "Player is currently vanished - either vanish yourself or use /tpa"
                                ),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, player) -> {
                                            Player sender = (Player) context.getSender();
                                            if (sender.hasPermission(VanishManager.VANISH_SEE_PERMISSION)) {
                                                return true;
                                            }
                                            AdminModule adminModule = basicsModule.getPlugin().getModule(AdminModule.class);
                                            if (adminModule != null) {
                                                return !adminModule.getVanishManager().isVanished(player);
                                            }
                                            return true;
                                        },
                                        "Player has teleportation disabled"
                                )
                        ),
                        ArgumentDescription.of("player")
                );
    }
}
