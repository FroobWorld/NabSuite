package com.froobworld.nabsuite.modules.discord.bot.chat;

import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.utils.DiscordUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Minecraft2DiscordBridge implements Listener {
    private final DiscordModule discordModule;

    public Minecraft2DiscordBridge(DiscordModule discordModule) {
        this.discordModule = discordModule;
        Bukkit.getPluginManager().registerEvents(this, discordModule.getPlugin());
        startup();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(discordModule.getPlugin(), this::updateStatus, 6000, 12000); // update every 10 minutes to avoid throttling
    }

    public void updateStatus() {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        String topic = String.format("%d/%d players online | %d unique players this map | Last status update: <t:%s:R>",
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers(),
                Bukkit.getOfflinePlayers().length,
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        );
        channel.getManager().setTopic(topic).queue();
    }

    public void startup() {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        channel.sendMessage(":white_check_mark: **Server has started**").queue();
    }

    public void shutdown() {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        try {
            channel.sendMessage(":octagonal_sign: **Server has stopped**").complete(true);
        } catch (RateLimitedException ignored) {}
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onChat(AsyncChatEvent event) {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        String messageFormat = discordModule.getDiscordConfig().messageFormats.minecraftToDiscord.get();
        String messageText = messageFormat
                .replace("<username>", event.getPlayer().getName())
                .replace("<message>", PlainTextComponentSerializer.plainText().serialize(event.message()));

        channel.sendMessage(DiscordUtils.escapeMarkdown(messageText))
                .allowedMentions(Collections.emptySet())
                .queue();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent event) {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        if (!event.getPlayer().hasPlayedBefore()) {
            channel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setAuthor(event.getPlayer().getName() + " joined the server for the first time", null, DiscordUtils.getHeadUrl(event.getPlayer().getUniqueId(), 128))
                            .build()
            ).queue();
        } else {
            channel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setAuthor(event.getPlayer().getName() + " joined the server", null, DiscordUtils.getHeadUrl(event.getPlayer().getUniqueId(), 128))
                            .build()
            ).queue();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        channel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.RED)
                        .setAuthor(event.getPlayer().getName() + " left the server", null, DiscordUtils.getHeadUrl(event.getPlayer().getUniqueId(), 128))
                        .build()
        ).queue();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onAdvancement(PlayerAdvancementDoneEvent event) {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }

        // ignore recipes and other advancements that don't show in chat
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipe/") || key.contains("recipes/")) {
            return;
        }
        if (event.message() == null) {
            return;
        }

        String advancementName = PlainTextComponentSerializer.plainText().serialize(event.getAdvancement().displayName());
        channel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setAuthor(event.getPlayer().getName() + " has made advancement " + advancementName, null, DiscordUtils.getHeadUrl(event.getPlayer().getUniqueId(), 128))
                        .build()
        ).queue();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onDeath(PlayerDeathEvent event) {
        TextChannel channel = discordModule.getDiscordBot().getChatChannel();
        if (channel == null) {
            return;
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage == null) {
            return;
        }
        String deathMessageString =  PlainTextComponentSerializer.plainText().serialize(deathMessage);
        channel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.BLACK)
                        .setAuthor(deathMessageString, null, DiscordUtils.getHeadUrl(event.getPlayer().getUniqueId(), 128))
                        .build()
        ).queue();
    }

}
