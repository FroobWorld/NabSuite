package com.froobworld.nabsuite.modules.discord.bot.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.bot.linking.AccountLinkManager;
import com.froobworld.nabsuite.modules.discord.config.DiscordConfig;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordCommandBridge extends ListenerAdapter {

    DiscordModule discordModule;
    AdminModule adminModule;
    AccountLinkManager accountLinkManager;
    Map<String, DiscordCommand> commands = new HashMap<>();

    public DiscordCommandBridge(DiscordModule discordModule, AccountLinkManager accountLinkManager) {
        this.discordModule = discordModule;
        this.accountLinkManager = accountLinkManager;
        this.adminModule = discordModule.getPlugin().getModule(AdminModule.class);

        syncCommands();
        discordModule.getDiscordBot().getJda().addEventListener(this);
    }

    private void syncCommands() {
        Map<String, cloud.commandframework.Command<CommandSender>> nabCommands = new HashMap<>();
        for (cloud.commandframework.Command<CommandSender> command: discordModule.getPlugin().getCommandManager().getCommands()) {
            String commandName = command.getArguments().stream()
                    .filter(a -> StaticArgument.class.equals(a.getClass()))
                    .map(CommandArgument::getName)
                    .collect(Collectors.joining(" "));
            nabCommands.put(commandName.toLowerCase(), command);
        }

        CommandMap bukkitCommands = Bukkit.getCommandMap();

        DiscordConfig.Commands config = discordModule.getDiscordConfig().commands;

        Map<String, DiscordCommand> commandMap = new HashMap<>();

        for (String cmd: config.enabled.get()) {
            DiscordConfig.CommandSettings settings = config.settings.of(cmd);
            String overrideName = settings.overrideName.get().isEmpty() ? cmd : settings.overrideName.get();
            String overrideMain = overrideName.contains(" ") ? overrideName.split(" ", 2)[0] : overrideName;
            String overrideSub = overrideName.contains(" ") ? overrideName.split(" ", 2)[1] : null;
            String mainCommand = cmd.contains(" ") ? cmd.split(" ", 2)[0] : cmd;

            DiscordCommand command = null;
            if (nabCommands.containsKey(cmd.toLowerCase())) {
                command = new DiscordCommand(this, cmd, settings, nabCommands.get(cmd.toLowerCase()));
            } else {
                org.bukkit.command.Command bukkitCommand = bukkitCommands.getCommand(mainCommand);
                if (bukkitCommand != null) {
                    command = new DiscordCommand(this, cmd, settings, bukkitCommand.getDescription());
                }
            }

            if (command == null) {
                discordModule.getPlugin().getSLF4JLogger().error("DiscordCommandBridge: Command not found - {}", cmd);
                continue;
            }

            DiscordCommand parent = commandMap.get(overrideMain);
            if (overrideSub == null) {
                if (parent != null) {
                    discordModule.getPlugin().getSLF4JLogger().error("DiscordCommandBridge: Duplicate command or mixing of subcommands and commands - {}", cmd);
                    continue;
                }
                commandMap.put(overrideMain, command);
            } else {
                if (parent == null) {
                    parent = new DiscordCommand(this, overrideMain, null, overrideMain);
                    commandMap.put(overrideMain, parent);
                } else if (parent.config != null) {
                    discordModule.getPlugin().getSLF4JLogger().error("DiscordCommandBridge: Mixing of subcommands and commands not allowed - {}", cmd);
                    continue;
                }
                parent.addSubCommand(overrideSub, command);
            }

        }

        discordModule.getDiscordBot().getJda().updateCommands()
                .addCommands(commandMap.entrySet().stream().map(v -> v.getValue().getCommandData(v.getKey())).toList())
                .queue(commands -> {
                    if (commands != null) {
                        for (Command command : commands) {
                            DiscordCommand cmd = commandMap.get(command.getName());
                            if (cmd != null) {
                                this.commands.put(command.getId(), cmd);
                            }
                        }
                    }
                });
    }

    private DiscordCommand getCommand(String commandId, String subCommandName) {
        DiscordCommand command = commands.get(commandId);
        if (command != null && subCommandName != null && !subCommandName.isEmpty()) {
            return command.getSubCommand(subCommandName);
        }
        return command;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild() || event.getGuild() == null) {
            event.reply("Commands are not allowed in DM").setEphemeral(true).queue(msg -> {
                msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS);
            });
            return;
        }
        if (!event.getGuild().getId().equals(discordModule.getDiscordConfig().guildId.get())) {
            event.reply("Commands are not allowed in this server").setEphemeral(true).queue(msg -> {
                msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS);
            });
            return;
        }
        DiscordCommand command = getCommand(event.getCommandId(), event.getSubcommandName());
        if (command == null) {
            return;
        }
        event.deferReply(command.isEphemeral()).queue(hook -> {
            PlayerIdentity player = accountLinkManager.getLinkedMinecraftAccount(event.getUser());
            if (player == null) {
                hook.editOriginal("You must link your account to use commands. Use this command in-game: /discord link")
                        .queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
            DiscordCommandSender sender = new DiscordCommandSender(discordModule.getPlugin(), player, hook);
            Bukkit.getScheduler().runTask(discordModule.getPlugin(), () -> command.execute(sender, event));
        });
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild() || event.getGuild() == null) {
            event.replyChoices().queue();
            return;
        }
        if (!event.getGuild().getId().equals(discordModule.getDiscordConfig().guildId.get())) {
            event.replyChoices().queue();
            return;
        }
        DiscordCommand command = getCommand(event.getCommandId(), event.getSubcommandName());
        if (command == null) {
            return;
        }

        PlayerIdentity player = accountLinkManager.getLinkedMinecraftAccount(event.getUser());
        if (player == null) {
            event.replyChoices().queue();
            return;
        }
        DiscordCommandSender sender = new DiscordCommandSender(discordModule.getPlugin(), player);
        Bukkit.getScheduler().runTask(discordModule.getPlugin(), () -> command.autocomplete(sender, event));
    }

}
