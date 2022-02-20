package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.command.CommandSender;

public class MotdCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public MotdCommand(BasicsModule basicsModule) {
        super(
                "motd",
                "Display the message of the day.",
                "nabsuite.command.motd",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        basicsModule.getMotdManager().getMotd().forEach(context.getSender()::sendMessage);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
