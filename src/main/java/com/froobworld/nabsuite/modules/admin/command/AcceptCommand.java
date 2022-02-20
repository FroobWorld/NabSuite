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

public class AcceptCommand extends NabCommand {
    private final AdminModule adminModule;

    public AcceptCommand(AdminModule adminModule) {
        super(
                "accept",
                "Accept a player's application to be removed from the grey list.",
                "nabsuite.command.accept",
                CommandSender.class,
                "allow"
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        adminModule.getGreylistManager().getGreylistEnforcer().setGreylisted(player.getUuid(), false);
        context.getSender().sendMessage(
                Component.text("Request accepted.")
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
                        true,
                        new ArgumentPredicate<>(
                                true,
                                (context, player) -> adminModule.getGreylistManager().getGreylistData(player.getUuid()).hasRequestedRemoval(),
                                "That player has not requested to be removed from the grey list"
                        )
                ));
    }
}
