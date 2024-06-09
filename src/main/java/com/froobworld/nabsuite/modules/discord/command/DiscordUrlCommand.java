package com.froobworld.nabsuite.modules.discord.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DiscordUrlCommand extends NabCommand {
    private final DiscordModule discordModule;

    public DiscordUrlCommand(DiscordModule discordModule) {
        super(
                "url",
                "Get a Discord invite link.",
                "nabsuite.command.discord.link",
                CommandSender.class
        );
        this.discordModule = discordModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        String url = discordModule.getDiscordConfig().inviteUrl.get();
        context.getSender().sendMessage(
                Component.text("Join us on Discord: ")
                        .append(Component.text(url))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url))
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/discord url";
    }
}
