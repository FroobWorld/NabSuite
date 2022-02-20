package com.froobworld.nabsuite.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public abstract class NabParentCommand extends NabCommand {

    public NabParentCommand(String commandName, String description, String permission, Class<? extends CommandSender> senderType, String... aliases) {
        super(commandName, description, permission, senderType, aliases);
    }

    @Override
    public final void execute(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        for (NabCommand childCommand : childCommands) {
            if (childCommand.senderType.isAssignableFrom(sender.getClass()) && sender.hasPermission(childCommand.permission)) {
                sender.sendMessage(Component.text(childCommand.getUsage()));
            }
        }
    }

    @Override
    public final Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
