package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.DurationArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.Punishments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class MuteCommand extends NabCommand {
    private final AdminModule adminModule;

    public MuteCommand(AdminModule adminModule) {
        super(
                "mute",
                "Mute a player.",
                "nabsuite.command.mute",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        long duration = context.get("duration");
        Optional<String> reason = context.getOptional("reason");
        Punishments punishments = adminModule.getPunishmentManager().getPunishments(player.getUuid());
        punishments.lock.writeLock().lock();
        try {
            if (punishments.getMutePunishment() != null) { // protect against race condition
                context.getSender().sendMessage(
                        Component.text("That player is already muted.", NamedTextColor.RED)
                );
                return;
            }
            adminModule.getPunishmentManager().getMuteEnforcer().mute(
                    player,
                    context.getSender(),
                    reason.orElse(null),
                    duration
            );
            context.getSender().sendMessage(
                    Component.text("Player muted.").color(NamedTextColor.YELLOW)
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
                                (context, player) -> adminModule.getPunishmentManager().getPunishments(player.getUuid()).getMutePunishment() == null,
                                "That player is already muted."
                        )
                ))
                .argument(new DurationArgument<>(
                        true,
                        "duration"
                ))
                .argument(StringArgument.<CommandSender>newBuilder("reason")
                        .greedy()
                        .asOptional()
                );
    }
}
