package com.froobworld.nabsuite.modules.discord.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordLinkCommand extends NabCommand {
    private final DiscordModule discordModule;

    public DiscordLinkCommand(DiscordModule discordModule) {
        super(
                "link",
                "Link your Discord account to Minecraft.",
                "nabsuite.command.discord.link",
                Player.class
        );
        this.discordModule = discordModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        int code = discordModule.getDiscordBot().getAccountLinkManager().initiateLink((Player) context.getSender());
        discordModule.getDiscordBot().getJda().getSelfUser().getName();
        context.getSender().sendMessage(
                Component.text("Message code ", NamedTextColor.YELLOW)
                        .append(Component.text(code, NamedTextColor.RED))
                        .append(Component.text(" to bot ", NamedTextColor.YELLOW))
                        .append(Component.text(discordModule.getDiscordBot().getJda().getSelfUser().getName(), NamedTextColor.RED))
                        .append(Component.text(" to link your account.", NamedTextColor.YELLOW))
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/discord link";
    }
}
