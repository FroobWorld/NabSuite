package com.froobworld.nabsuite.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.froobworld.nabsuite.command.suggestion.CaseInsensitiveFilteringCommandSuggestionProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

// TODO - replace with own command library
public class NabCommandManager extends PaperCommandManager<CommandSender> {

    public NabCommandManager(Plugin plugin) throws Exception {
        super(
                plugin,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );
        if (queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            //registerBrigadier();
        }
        setCommandSuggestionProcessor(new CaseInsensitiveFilteringCommandSuggestionProcessor<>());
        new MinecraftExceptionHandler<CommandSender>()
                .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, exception -> Component.text("/" + ((InvalidSyntaxException) exception).getCorrectSyntax()))
                .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, exception -> Component.text("You do not have permission to use this command.", NamedTextColor.RED))
                .withInvalidSenderHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(Function.identity())
                .apply(this, sender -> sender);
    }

    public void registerCommand(NabCommand nabCommand) {
        Command.Builder<CommandSender> builder = commandBuilder(
                nabCommand.commandName,
                ArgumentDescription.of(nabCommand.description),
                nabCommand.aliases)
                .meta(CommandMeta.DESCRIPTION, nabCommand.description)
                .handler(nabCommand::execute)
                .senderType(nabCommand.senderType)
                .permission(nabCommand.permission);
        builder = nabCommand.populateBuilder(builder);
        command(builder.build());
        registerChildren(builder, nabCommand);
    }

    private void registerChildren(Command.Builder<CommandSender> parentBuilder, NabCommand parentCommand) {
        for (NabCommand childCommand : parentCommand.childCommands) {
            Command.Builder<CommandSender> childBuilder = parentBuilder
                    .literal(childCommand.commandName, childCommand.aliases)
                    .meta(CommandMeta.DESCRIPTION, childCommand.description)
                    .handler(childCommand::execute)
                    .senderType(childCommand.senderType)
                    .permission(childCommand.permission);
            childBuilder = childCommand.populateBuilder(childBuilder);
            command(childBuilder.build());
            registerChildren(childBuilder, childCommand);
        }
    }

}
