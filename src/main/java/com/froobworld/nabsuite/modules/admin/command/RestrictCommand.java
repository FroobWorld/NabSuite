package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.Punishments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class RestrictCommand extends NabCommand {
    private final AdminModule adminModule;

    public RestrictCommand(AdminModule adminModule) {
        super(
                "restrict",
                "Restrict a player from building near other players.",
                "nabsuite.command.restrict",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        String reason = context.get("reason");
        Punishments punishments = adminModule.getPunishmentManager().getPunishments(player.getUuid());
        punishments.lock.writeLock().lock();
        try {
            if (punishments.getMutePunishment() != null) { // protect against race condition
                context.getSender().sendMessage(
                        Component.text("That player is already restricted.", NamedTextColor.RED)
                );
                return;
            }
            adminModule.getPunishmentManager().getRestrictionEnforcer().restrict(player, context.getSender(), reason);
            context.getSender().sendMessage(
                    Component.text(player.getLastName() + " has been restricted.", NamedTextColor.YELLOW)
            );
        } finally {
            punishments.lock.writeLock().unlock();
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true,
                        new ArgumentPredicate<>(
                                true,
                                (sender, playerIdentity) -> {
                                    return adminModule.getPunishmentManager().getPunishments(playerIdentity.getUuid()).getRestrictionPunishment() == null;
                                },
                                "That player is already restricted"
                        )
                ))
                .argument(new StringArgument<>(
                        true,
                        "reason",
                        true
                ));
    }
}
