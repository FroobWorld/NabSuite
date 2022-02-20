package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class GreylistCheckCommand extends NabCommand {
    private final AdminModule adminModule;

    public GreylistCheckCommand(AdminModule adminModule) {
        super(
                "greylist",
                "Check if a player is on the grey list.",
                "nabsuite.command.greylist.check",
                CommandSender.class,
                "graylist", "gl"
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        if (adminModule.getGreylistManager().getGreylistData(player.getUuid()).isGreylisted()) {
            context.getSender().sendMessage(
                    Component.text("That player is on the grey list.")
                            .color(NamedTextColor.YELLOW)
            );
        } else {
            context.getSender().sendMessage(
                    Component.text("That player is not on the grey list.")
                            .color(NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .literal("check")
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true
                ));
    }
}
