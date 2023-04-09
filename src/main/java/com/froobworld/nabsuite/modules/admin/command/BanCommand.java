package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.Punishments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class BanCommand extends NabCommand {
    private final AdminModule adminModule;

    public BanCommand(AdminModule adminModule) {
        super(
                "ban",
                "Ban a player from the server.",
                "nabsuite.command.ban",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        Optional<String> reason = context.getOptional("reason");
        Punishments punishments = adminModule.getPunishmentManager().getPunishments(player.getUuid());
        punishments.lock.writeLock().lock();
        try {
            if (punishments.getBanPunishment() != null) { // protect against race condition
                context.getSender().sendMessage(
                        Component.text("That player is already banned.", NamedTextColor.RED)
                );
                return;
            }
            adminModule.getPunishmentManager().getBanEnforcer().ban(
                    player,
                    context.getSender(),
                    reason.orElse(null),
                    -1
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
                                (context, player) -> adminModule.getPunishmentManager().getPunishments(player.getUuid()).getBanPunishment() == null,
                                "That player is already banned."
                        )
                ))
                .argument(StringArgument.<CommandSender>newBuilder("reason")
                        .greedy()
                        .asOptional()
                );
    }
}
