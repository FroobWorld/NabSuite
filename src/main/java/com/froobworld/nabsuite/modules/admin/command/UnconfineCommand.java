package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.JailPunishment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class UnconfineCommand extends NabCommand {
    private final AdminModule adminModule;

    public UnconfineCommand(AdminModule adminModule) {
        super(
                "unconfine",
                "Unconfine a confined player.",
                "nabsuite.command.unconfine",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        adminModule.getPunishmentManager().getJailEnforcer().unjail(player, context.getSender());
        context.getSender().sendMessage(
                Component.text(player.getLastName() + " has been unconfined.", NamedTextColor.YELLOW)
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
                                    return jailPunishment != null && jailPunishment.isConfinement();
                                },
                                "That player is not confined"
                        )
                ));
    }
}
