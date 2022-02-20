package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class UnbanCommand extends NabCommand {
    private final AdminModule adminModule;

    public UnbanCommand(AdminModule adminModule) {
        super(
                "unban",
                "Unban a player from the server.",
                "nabsuite.command.unban",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity playerIdentity = context.get("player");
        adminModule.getPunishmentManager().getBanEnforcer().unban(playerIdentity.getUuid(), context.getSender());
        context.getSender().sendMessage(
                Component.text(playerIdentity.getLastName())
                .append(Component.text(" has been unbanned."))
                .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        false,
                        new ArgumentPredicate<>(
                                true,
                                (context, playerIdentity) -> adminModule.getPunishmentManager().getPunishments(playerIdentity.getUuid()).getBanPunishment() != null,
                                "Player is not banned."
                        )
                ));
    }
}
