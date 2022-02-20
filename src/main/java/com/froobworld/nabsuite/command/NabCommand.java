package com.froobworld.nabsuite.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class NabCommand {
    protected final String commandName;
    protected final String description;
    protected final String permission;
    protected final Class<? extends CommandSender> senderType;
    protected final String[] aliases;
    protected final List<NabCommand> childCommands;

    public NabCommand(String commandName, String description, String permission, Class<? extends CommandSender> senderType, String... aliases) {
        this.commandName = commandName;
        this.description = description;
        this.permission = permission;
        this.senderType = senderType;
        this.aliases = aliases;
        childCommands = new ArrayList<>();
    }

    public abstract void execute(CommandContext<CommandSender> context);

    public abstract Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder);

    public String getUsage() {
        return "";
    }

}
