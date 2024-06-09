package com.froobworld.nabsuite.modules.discord.command;

import com.froobworld.nabsuite.command.NabParentCommand;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DiscordCommand extends NabParentCommand {

    public DiscordCommand(DiscordModule discordModule) {
        super(
                "discord",
                "Join us on discord.",
                "nabsuite.command.discord",
                CommandSender.class
        );
        childCommands.addAll(List.of(
                new DiscordUrlCommand(discordModule),
                new DiscordLinkCommand(discordModule)
        ));
    }

}
