package com.froobworld.nabsuite.modules.discord.bot.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.modules.admin.command.argument.JailArgument;
import com.froobworld.nabsuite.modules.discord.config.DiscordConfig;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class DiscordCommand {
    protected final String commandName;
    protected final String description;
    protected final DiscordCommandBridge bridge;
    protected final DiscordConfig.CommandSettings config;
    protected final Map<String, DiscordCommand> subCommands = new HashMap<>();
    protected cloud.commandframework.Command<CommandSender> cloudCommand;

    public DiscordCommand(DiscordCommandBridge bridge, String name, DiscordConfig.CommandSettings settings, cloud.commandframework.Command<CommandSender> command) {
        this(bridge, name, settings, command.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, ""));
        this.cloudCommand = command;
    }

    public DiscordCommand(DiscordCommandBridge bridge, String command, DiscordConfig.CommandSettings settings, String description) {
        this.bridge = bridge;
        this.commandName = command;
        this.description = description != null && !description.isEmpty() ? description : commandName;
        this.config = settings;
    }

    public String getName() {
        return commandName;
    }

    public boolean isEphemeral() {
        return config == null || !config.publicReply.get();
    }

    public void addSubCommand(String name, DiscordCommand command) {
        subCommands.put(name, command);
    }

    public DiscordCommand getSubCommand(String name) {
        return subCommands.get(name);
    }

    public CommandData getCommandData(String name) {
        CommandDataImpl data = new CommandDataImpl(name, description);
        data.setContexts(EnumSet.of(InteractionContextType.GUILD))
                .setIntegrationTypes(EnumSet.of(IntegrationType.GUILD_INSTALL))
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);

        data.addSubcommands(subCommands.entrySet().stream()
                .map(sub -> sub.getValue().getSubcommandData(sub.getKey()))
                .toList());

        if (data.getSubcommands().isEmpty()) {
            if (cloudCommand != null) {
                data.addOptions(cloudCommand.getArguments()
                        .stream()
                        .filter(a -> !a.getClass().isAssignableFrom(StaticArgument.class))
                        .map(this::buildOption)
                        .toList());
            } else {
                data.addOption(OptionType.STRING, "arguments", "arguments", false, true);
            }
        }
        return data;
    }

    private SubcommandData getSubcommandData(String name) {
        SubcommandData data = new SubcommandData(name, description);
        if (cloudCommand != null) {
            data.addOptions(cloudCommand.getArguments()
                    .stream()
                    .filter(a -> !a.getClass().isAssignableFrom(StaticArgument.class))
                    .map(this::buildOption)
                    .toList());
        } else {
            data.addOption(OptionType.STRING, "arguments", "arguments", false, true);
        }
        return data;
    }

    private OptionData buildOption(CommandArgument<CommandSender, ?> arg) {
        OptionData data = new OptionData(OptionType.STRING, arg.getName().toLowerCase(), arg.getName(), arg.isRequired(), true);

        if (JailArgument.class.equals(arg.getClass())) {
            // List of jails doesn't change often enough for dynamic autocomplete.
            // Update command with all available options once on startup
            data.setAutoComplete(false)
                    .addChoices(
                            arg.getSuggestionsProvider().apply(new CommandContext<>(Bukkit.getConsoleSender(), bridge.discordModule.getPlugin().getCommandManager()), "")
                                    .stream()
                                    .map(value -> new Command.Choice(value, value))
                                    .toList()
                    );
        } else if (StringArgument.class.equals(arg.getClass())) {
            data.setAutoComplete(false);
        }

        return data;
    }

    private String getCommandLine(Function<String, OptionMapping> options) {
        return getCommandLine(options, null, null);
    }

    private String getCommandLine(Function<String, OptionMapping> options, String currentField, String currentValue) {
        StringBuilder cmd = new StringBuilder(commandName);
        if (cloudCommand != null) {
            for (CommandArgument<CommandSender, ?> arg : cloudCommand.getArguments()) {
                if (arg.getClass().isAssignableFrom(StaticArgument.class)) {
                    continue;
                }
                cmd.append(" ");
                OptionMapping opt = options.apply(arg.getName().toLowerCase());
                if (currentField != null && currentField.equals(arg.getName().toLowerCase())) {
                    cmd.append(currentValue);
                    break;
                }
                if (opt != null && !opt.getAsString().isEmpty()) {
                    cmd.append(opt.getAsString());
                }
            }
        } else {
            if (currentField != null && currentField.equals("arguments")) {
                cmd.append(" ").append(currentValue);
            } else {
                OptionMapping opt = options.apply("arguments");
                if (opt != null && !opt.getAsString().isEmpty()) {
                    cmd.append(" ").append(opt.getAsString());
                }
            }
        }
        return cmd.toString();
    }

    public void execute(@NotNull DiscordCommandSender sender, @NotNull SlashCommandInteractionEvent event) {
        try {
            String command = getCommandLine(event::getOption);
            bridge.discordModule.getPlugin().getSLF4JLogger().info(
                    "DiscordCommandBridge: {} issued server command: /{}",
                    sender.getName(),
                    command
            );
            Bukkit.dispatchCommand(sender, command);
        } catch (Throwable e) {
            sender.replyError(e.getMessage());
        }
    }

    public void autocomplete(@NotNull DiscordCommandSender sender, @NotNull CommandAutoCompleteInteractionEvent event) {
        try {
            AutoCompleteQuery q = event.getFocusedOption();
            String commandLine = getCommandLine(event::getOption, q.getName(), q.getValue());
            List<String> suggestions = Bukkit.getCommandMap().tabComplete(sender, commandLine);
            if (suggestions == null || suggestions.isEmpty()) {
                event.replyChoices().queue();
                return;
            }
            List<String> current = Arrays.stream(q.getValue().split(" ")).toList();
            String prefix = current.size() == 1 ? "" : String.join(" ", current.subList(0, current.size() - 2));
            event.replyChoices(suggestions.stream().map(s -> new Command.Choice(prefix + s, prefix + s)).toList()).queue();

        } catch (Throwable e) {
            event.replyChoices().queue();
        }
    }

}
