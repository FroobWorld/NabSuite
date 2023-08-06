package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class WarnCommand extends NabCommand {
    private final AdminModule adminModule;

    public WarnCommand(AdminModule adminModule) {
        super(
                "warn",
                "Send a player a warning.",
                "nabsuite.command.warn",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        String message = context.get("message");
        adminModule.getNoteManager().sendWarning(player, ConsoleUtils.getSenderUUID(context.getSender()), message);
        context.getSender().sendMessage(Component.text("Warning sent.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true
                ))
                .argument(new StringArgument<>(
                        true,
                        "message",
                        true
                ));
    }
}
