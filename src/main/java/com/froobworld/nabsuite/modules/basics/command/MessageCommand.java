package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.command.CommandSender;

public class MessageCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public MessageCommand(BasicsModule basicsModule) {
        super("message",
                "Send a private message to another user.",
                "nabsuite.command.message",
                CommandSender.class,
                "msg", "m", "whisper", "w");
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        basicsModule.getMessageCentre().sendMessage(context.getSender(), context.get("recipient"), context.get("message"));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerArgument<>(true, "recipient"), ArgumentDescription.of("recipient"))
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("message"));
    }
}
