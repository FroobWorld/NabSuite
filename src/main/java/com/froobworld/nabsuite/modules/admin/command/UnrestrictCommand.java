package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class UnrestrictCommand extends NabCommand {
    private final AdminModule adminModule;

    public UnrestrictCommand(AdminModule adminModule) {
        super(
                "unrestrict",
                "Unrestrict a restricted player.",
                "nabsuite.command.unrestrict",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        adminModule.getPunishmentManager().getRestrictionEnforcer().unrestrict(player, ConsoleUtils.getSenderUUID(context.getSender()));
        context.getSender().sendMessage(
                Component.text(player.getLastName() + " has been unrestricted.", NamedTextColor.YELLOW)
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
                                    return adminModule.getPunishmentManager().getPunishments(playerIdentity.getUuid()).getRestrictionPunishment() != null;
                                },
                                "That player is not restricted"
                        )
                ));
    }
}
