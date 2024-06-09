package com.froobworld.nabsuite.modules.discord;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.discord.bot.DiscordBot;
import com.froobworld.nabsuite.modules.discord.command.DiscordCommand;
import com.froobworld.nabsuite.modules.discord.config.DiscordConfig;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

public class DiscordModule extends NabModule {
    private DiscordConfig discordConfig;
    private DiscordBot discordBot;

    public DiscordModule(NabSuite nabSuite) {
        super(nabSuite, "discord");
    }

    @Override
    public void onEnable() {
        discordConfig = new DiscordConfig(this);
        try {
            discordConfig.load();
        } catch (Exception e) {
            getPlugin().getSLF4JLogger().error("Exception while loading config", e);
            Bukkit.getPluginManager().disablePlugin(getPlugin());
            return;
        }
        try {
            discordBot = new DiscordBot(this);
        } catch (Exception e) {
            getPlugin().getSLF4JLogger().error("Exception while starting Discord bot", e);
            // TODO shut down module
            return;
        }

        Lists.newArrayList(
                new DiscordCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    @Override
    public void onDisable() {
        discordBot.shutdown();
    }

    public DiscordConfig getDiscordConfig() {
        return discordConfig;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }
}
