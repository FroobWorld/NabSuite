package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.JailArgument;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import com.froobworld.nabsuite.modules.admin.punishment.JailPunishment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

public class ConfineCommand extends NabCommand {
    private final AdminModule adminModule;

    public ConfineCommand(AdminModule adminModule) {
        super(
                "confine",
                "Confine a player to a secure location.",
                "nabsuite.command.confine",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        Jail jail = context.get("jail");
        String reason = context.get("reason");
        adminModule.getPunishmentManager().getJailEnforcer().jail(player, context.getSender(), jail, reason, TimeUnit.DAYS.toMillis(1), true);
        context.getSender().sendMessage(
                Component.text(player.getLastName() + " has been confined.", NamedTextColor.YELLOW)
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
                                (sender, playerIdentity) -> {
                                    JailPunishment jailPunishment = adminModule.getPunishmentManager().getPunishments(playerIdentity.getUuid()).getJailPunishment();
                                    return jailPunishment == null;
                                },
                                "That player is already confined or jailed"
                        )
                ))
                .argument(new JailArgument<>(
                        true,
                        "jail",
                        adminModule.getJailManager()
                ))
                .argument(new StringArgument<>(
                        true,
                        "reason",
                        true
                ));
    }
}
