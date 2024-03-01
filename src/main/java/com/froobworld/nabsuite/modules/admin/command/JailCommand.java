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
import com.froobworld.nabsuite.modules.admin.command.argument.JailArgument;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class JailCommand extends NabCommand {
    private final AdminModule adminModule;

    public JailCommand(AdminModule adminModule) {
        super(
                "jail",
                "Jail a player.",
                "nabsuite.command.jail",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        Jail jail = context.get("jail");
        long duration = context.get("duration");
        Optional<String> reason = context.getOptional("reason");
        adminModule.getPunishmentManager().getJailEnforcer().jail(
                player,
                context.getSender(),
                jail,
                reason.orElse(null),
                duration,
                false
        );
        context.getSender().sendMessage(
                Component.text("Player jailed.").color(NamedTextColor.YELLOW)
        );
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
                                (context, player) -> adminModule.getPunishmentManager().getPunishments(player.getUuid()).getJailPunishment() == null,
                                "That player is already jailed."
                        )
                ))
                .argument(new JailArgument<>(
                        true,
                        "jail",
                        adminModule.getJailManager()
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
