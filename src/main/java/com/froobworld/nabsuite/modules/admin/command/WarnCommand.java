package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
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
        BasicsModule basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        basicsModule.getMailCentre().sendSystemMail(player.getUuid(), "You have received a warning with message \"" + message + "\".");
        adminModule.getNoteManager().createNote(player.getUuid(), ConsoleUtils.getSenderUUID(context.getSender()), "Warning sent with message \"" + message + "\".");
        if (player.asPlayer() != null) {
            player.asPlayer().sendMessage(
                    Component.newline()
                            .append(Component.text("You have received a warning with the following message:", NamedTextColor.RED))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(Component.text(message, NamedTextColor.GOLD))
                            .append(Component.newline())
            );
        }
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
