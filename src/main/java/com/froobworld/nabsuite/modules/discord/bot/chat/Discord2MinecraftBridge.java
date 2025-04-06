package com.froobworld.nabsuite.modules.discord.bot.chat;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.util.PlayerList;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.util.ComponentUtils;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;

import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;

public class Discord2MinecraftBridge extends ListenerAdapter {
    private final DiscordModule discordModule;

    public Discord2MinecraftBridge(DiscordModule discordModule) {
        this.discordModule = discordModule;
        discordModule.getDiscordBot().getJda().addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }
        if (!event.isFromGuild() || !event.getChannel().equals(discordModule.getDiscordBot().getChatChannel())) {
            return;
        }
        if (event.getMessage().getContentRaw().equalsIgnoreCase("playerlist")) {
            handlePlayerList(event.getMessage());
            return;
        }
        PlayerIdentity linkedAccount = discordModule.getDiscordBot().getAccountLinkManager().getLinkedMinecraftAccount(event.getAuthor());
        if (linkedAccount == null) {
            event.getMessage().delete().queue();
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Please link your Minecraft and Discord accounts to use the chat channel by running `/discord link` in game.").queue();
            });
            return;
        }
        //noinspection UnnecessaryUnicodeEscape
        String messageText = EmojiParser.parseToAliases((event.getMessage().getContentDisplay()))
                .replaceAll("[\\p{C}\\p{So}\uFE00-\uFE0F\\x{E0100}-\\x{E01EF}]+", " ") // remove non-printable characters
                .replaceAll(" +", " ") // remove excess spaces
                .trim();
        if (messageText.isEmpty()) {
            return;
        }
        if (messageText.length() > 300) {
            event.getMessage().replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.RED)
                            .setAuthor("Your message is too long and cannot be relayed.")
                            .build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        Bukkit.getScheduler().runTask(discordModule.getPlugin(), () -> {
            String messageFormat = discordModule.getDiscordConfig().messageFormats.discordToMinecraft.get();
            Component message = MiniMessage.miniMessage().deserialize(messageFormat,
                    TagResolver.builder()
                            .tag("username", Tag.inserting(Component.text(linkedAccount.getLastName())))
                            .tag("message", Tag.inserting(ComponentUtils.clickableUrls(Component.text(messageText))))
                            .build()
            );
            AdminModule adminModule = discordModule.getPlugin().getModule(AdminModule.class);
            if (adminModule != null) {
                if (adminModule.getPunishmentManager().getMuteEnforcer().testMute(linkedAccount.getUuid())) {
                    event.getMessage().delete().queue();
                    event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage("Muted players are unable to send messages to the Minecraft chat.").queue();
                    });
                    return;
                }
                if (adminModule.getPunishmentManager().getBanEnforcer().testBan(linkedAccount.getUuid()) != null) {
                    event.getMessage().delete().queue();
                    event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage("Banned players are unable to send messages to the Minecraft chat.").queue();
                    });
                    return;
                }
            }
            BasicsModule basicsModule = discordModule.getPlugin().getModule(BasicsModule.class);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (basicsModule != null) {
                    if (basicsModule.getPlayerDataManager().getIgnoreManager().isIgnoring(player.getUniqueId(), linkedAccount.getUuid())) {
                        continue;
                    }
                }
                player.sendMessage(message);
            }
            Bukkit.getConsoleSender().sendMessage(message);
            DynmapAPI dynmapAPI = discordModule.getPlugin().getHookManager().getDynmapHook().getDynmapAPI();
            if (dynmapAPI != null) {
                dynmapAPI.sendBroadcastToWeb("[Discord] " + linkedAccount.getLastName(), messageText);
            }
        });
    }

    private void handlePlayerList(Message message) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        String playerList = onlinePlayers.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        BasicsModule basicsModule = discordModule.getPlugin().getModule(BasicsModule.class);
        if (basicsModule != null) {
            playerList = PlainTextComponentSerializer.plainText().serialize(PlayerList.getPlayerListDecorated(basicsModule));
        }

        String playerListMessage;
        if (playerList.isEmpty()) {
            playerListMessage = "**```There are no players online.```**";
        } else {
            playerListMessage = String.format("**```Currently online (%d/%d)```**```%s```", onlinePlayers.size(), Bukkit.getMaxPlayers(), playerList);
        }
        message.reply(playerListMessage).mentionRepliedUser(false).queue();
    }

}
