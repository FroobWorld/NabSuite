package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.command.CommandSender;

public class ReplyCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ReplyCommand(BasicsModule basicsModule) {
        super("reply",
                "Reply to the last user to send you a message.",
                "nabsuite.command.reply",
                CommandSender.class,
                "r"
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        basicsModule.getMessageCentre().reply(context.getSender(), context.get("message"));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(StringArgument.greedy("message"));
    }
}
