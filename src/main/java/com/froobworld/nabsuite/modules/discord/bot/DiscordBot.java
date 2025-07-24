package com.froobworld.nabsuite.modules.discord.bot;

import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.bot.command.DiscordCommandBridge;
import com.froobworld.nabsuite.modules.discord.bot.chat.ChatBridge;
import com.froobworld.nabsuite.modules.discord.bot.command.DiscordCommandSender;
import com.froobworld.nabsuite.modules.discord.bot.linking.AccountLinkManager;
import com.froobworld.nabsuite.modules.discord.bot.syncer.DiscordSyncer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class DiscordBot {
    private final DiscordModule discordModule;
    private AccountLinkManager accountLinkManager;
    private ChatBridge chatBridge;
    private DiscordSyncer discordSyncer;
    private DiscordCommandBridge commandBridge;
    private JDA jda;
    private Webhook chatWebhook;
    private final Map<String, BiConsumer<DiscordCommandSender, ButtonInteractionEvent>> buttonHandlers = new HashMap<>();
    private final Map<String, BiConsumer<DiscordCommandSender, ModalInteractionEvent>> modalHandlers = new HashMap<>();

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
            if (discordModule.getDiscordConfig().useWebhook.get()) {
                TextChannel chatChannel = getChatChannel();
                if (chatChannel != null) {
                    List<Webhook> webhooks = chatChannel.retrieveWebhooks().complete();
                    if (webhooks != null) {
                        for (Webhook webhook : webhooks) {
                            if (webhook.getName().equals("NabsuiteChatBridge")) {
                                this.chatWebhook = webhook;
                            }
                        }
                        if (chatWebhook == null) {
                            this.chatWebhook = chatChannel.createWebhook("NabsuiteChatBridge").complete();
                        }
                    }
                }
            }
            setPresence();
            Bukkit.getScheduler().runTask(discordModule.getPlugin(), () -> {
                accountLinkManager = new AccountLinkManager(discordModule);
                chatBridge = new ChatBridge(discordModule);
                discordSyncer = new DiscordSyncer(discordModule);
                commandBridge = new DiscordCommandBridge(discordModule, accountLinkManager);
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
            if (chatWebhook != null) {
                chatWebhook.delete().submit(true).join();
            }
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

    public Webhook getChatWebhook() {
        return chatWebhook;
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

    public void addButtonListener(String action, BiConsumer<DiscordCommandSender, ButtonInteractionEvent> listener) {
        this.buttonHandlers.put(action, listener);
    }
    public void addButtonListener(String action, String permission, BiConsumer<DiscordCommandSender, ButtonInteractionEvent> listener) {
        addButtonListener(action, (sender, event) -> {
            if (permission == null || permission.isEmpty() || sender.hasPermission(permission)) {
                listener.accept(sender, event);
            } else {
                event.reply("You lack permission to execute this action")
                        .setEphemeral(true)
                        .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            }
        });
        this.buttonHandlers.put(action, listener);
    }
    public void addModalListener(String action, BiConsumer<DiscordCommandSender, ModalInteractionEvent> listener) {
        this.modalHandlers.put(action, listener);
    }
    public void addModalListener(String action, String permission, BiConsumer<DiscordCommandSender, ModalInteractionEvent> listener) {
        addModalListener(action, (sender, event) -> {
            if (permission == null || permission.isEmpty() || sender.hasPermission(permission)) {
                listener.accept(sender, event);
            } else {
                event.reply("You lack permission to execute this action")
                        .setEphemeral(true)
                        .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            }
        });
    }
    public BiConsumer<DiscordCommandSender, ButtonInteractionEvent> getButtonHandler(String action) {
            return this.buttonHandlers.get(action);
    }
    public BiConsumer<DiscordCommandSender, ModalInteractionEvent> getModalHandler(String action) {
        return this.modalHandlers.get(action);
    }

}
