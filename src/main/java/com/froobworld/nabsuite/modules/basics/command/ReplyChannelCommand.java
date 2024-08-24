package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyChannelCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ReplyChannelCommand(BasicsModule basicsModule) {
        super(
                "replychannel",
                "Reply to the last channel you messaged.",
                "nabsuite.command.replychannel",
                Player.class,
                "rc"
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        String message = context.get("message");

        basicsModule.getChatChannelManager().getMessageCentre().replyChannel(sender, message);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new StringArgument<>(
                        true,
                        "message",
                        true
                ));
    }
}
