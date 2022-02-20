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

public class GreylistAddCommand extends NabCommand {
    private final AdminModule adminModule;

    public GreylistAddCommand(AdminModule adminModule) {
        super(
                "greylist",
                "Add a player to the grey list.",
                "nabsuite.command.greylist.add",
                CommandSender.class,
                "graylist", "gl"
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        adminModule.getGreylistManager().getGreylistEnforcer().setGreylisted(player.getUuid(), true);
        context.getSender().sendMessage(
                Component.text("Player added to the grey list.")
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .literal("add")
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true,
                        new ArgumentPredicate<>(
                                true,
                                (context, player) -> !adminModule.getGreylistManager().getGreylistData(player.getUuid()).isGreylisted(),
                                "That player is already on the grey list."
                        )
                ));
    }
}
