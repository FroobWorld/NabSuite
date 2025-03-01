package com.froobworld.nabsuite.modules.discord.bot;

import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.bot.chat.ChatBridge;
import com.froobworld.nabsuite.modules.discord.bot.linking.AccountLinkManager;
import com.froobworld.nabsuite.modules.discord.bot.syncer.DiscordSyncer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import javax.security.auth.login.LoginException;

public class DiscordBot {
    private final DiscordModule discordModule;
    private AccountLinkManager accountLinkManager;
    private ChatBridge chatBridge;
    private DiscordSyncer discordSyncer;
    private JDA jda;

    public DiscordBot(DiscordModule discordModule) throws LoginException {
        this.discordModule = discordModule;
        tryInit();
        try {
            if (jda != null) {
                jda.awaitReady();
            }
        } catch (InterruptedException ignored) {}
    }

    private void tryInit() {
        if (jda == null) {
            try {
                jda = JDABuilder.createDefault(discordModule.getDiscordConfig().botToken.get())
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .build();
            } catch (Exception exception) {
                discordModule.getPlugin().getSLF4JLogger().warn("Failed to start Discord bot, retrying in 30 seconds.");
                Bukkit.getScheduler().runTaskLaterAsynchronously(discordModule.getPlugin(), this::tryInit, 600);
                return;
            }
            try {
                jda.awaitReady();
            } catch (InterruptedException ignored) {}
            setPresence();
            Bukkit.getScheduler().runTask(discordModule.getPlugin(), () -> {
                accountLinkManager = new AccountLinkManager(discordModule);
                chatBridge = new ChatBridge(discordModule);
                discordSyncer = new DiscordSyncer(discordModule);
            });
        }
    }

    private void setPresence() {
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("FroobWorld"));
    }

    public JDA getJda() {
        return jda;
    }

    public void shutdown() {
        if (jda != null) {
            chatBridge.shutdown();
            accountLinkManager.shutdown();
            jda.shutdown();
        }
    }

    public AccountLinkManager getAccountLinkManager() {
        return accountLinkManager;
    }

    public DiscordSyncer getDiscordSyncer() {
        return discordSyncer;
    }

    public Guild getGuild() {
        jda = getJda();
        if (jda == null) {
            return null;
        }
        return jda.getGuildById(discordModule.getDiscordConfig().guildId.get());
    }

    public TextChannel getChatChannel() {
        jda = getJda();
        if (jda == null) {
            return null;
        }
        return jda.getTextChannelById(discordModule.getDiscordConfig().channels.chat.get());
    }

    public TextChannel getStaffLogChannel() {
        jda = getJda();
        if (jda == null) {
            return null;
        }
        return jda.getTextChannelById(discordModule.getDiscordConfig().channels.staffLog.get());
    }

}
