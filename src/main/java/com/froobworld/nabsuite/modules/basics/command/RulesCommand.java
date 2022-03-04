package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class RulesCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public RulesCommand(BasicsModule basicsModule) {
        super(
                "rules",
                "Display the rules of the server.",
                "nabsuite.command.rules",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        basicsModule.getConfig().messages.rules.get().stream()
                .map(MiniMessage.miniMessage()::deserialize)
                .forEach(context.getSender()::sendMessage);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
