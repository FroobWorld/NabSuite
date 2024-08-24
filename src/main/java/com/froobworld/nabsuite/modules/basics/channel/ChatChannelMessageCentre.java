package com.froobworld.nabsuite.modules.basics.channel;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatChannelMessageCentre implements Listener {
    private final BasicsModule basicsModule;
    private final Map<UUID, ChatChannel> lastChannel = new HashMap<>();

    public ChatChannelMessageCentre(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
    }

    public void messageChannel(Player sender, ChatChannel channel, String message) {
        if (!channel.hasUserRights(sender)) {
            throw new IllegalArgumentException("Player does not have user rights.");
        }
        AdminModule adminModule = basicsModule.getPlugin().getModule(AdminModule.class);
        if (adminModule != null) {
            if (adminModule.getPunishmentManager().getMuteEnforcer().testMute(sender, true)) {
                return;
            }
        }
        if (!channel.isJoined(sender.getUniqueId())) {
            sender.sendMessage(Component.text(
                    "You must join the channel before you can message it.", NamedTextColor.RED
            ));
        }
        if (!channel.hasUserRights(sender)) {
            sender.sendMessage(Component.text(
                    "You do not have permission to message that channel.", NamedTextColor.RED
            ));
        }
        String format = basicsModule.getConfig().chatChannelFormat.get();
        Component component = MiniMessage.miniMessage().deserialize(
                format,
                TagResolver.resolver("display_name", Tag.inserting(sender.displayName())),
                TagResolver.resolver("channel", Tag.inserting(Component.text(channel.getName()))),
                TagResolver.resolver("message", Tag.inserting(Component.text(message)))
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (channel.isJoined(player.getUniqueId()) && channel.hasUserRights(player)) {
                if (basicsModule.getPlayerDataManager().getIgnoreManager().isIgnoring(player, sender)) {
                    continue;
                }
                player.sendMessage(component);
            }
        }
        lastChannel.put(sender.getUniqueId(), channel);
    }

    public void replyChannel(Player sender, String message) {
        ChatChannel lastChannel = this.lastChannel.get(sender.getUniqueId());
        if (lastChannel == null) {
            sender.sendMessage(Component.text(
                    "There is no channel to reply to.", NamedTextColor.RED
            ));
            return;
        }
        messageChannel(sender, lastChannel, message);
    }

    public void sendJoinChannelsMessage(Player sender, ChatChannel... channels) {
        Component channelPrefix = Component.empty();
        for (ChatChannel channel : channels) {
            channelPrefix = channelPrefix.append(Component.text(
                    "<" + channel.getName() + "> ", NamedTextColor.GOLD
            ));
        }
        Component channelJoinMessage = channelPrefix.append(Component.text(
                sender.getName() + " joined the channel.", NamedTextColor.WHITE
        ));
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ChatChannel channel : channels) {
                if (channel.isJoined(player.getUniqueId()) && channel.hasUserRights(player)) {
                    player.sendMessage(channelJoinMessage);
                    break;
                }
            }
        }
    }

    public void sendLeaveChannelsMessage(Player sender, ChatChannel... channels) {
        Component channelPrefix = Component.empty();
        for (ChatChannel channel : channels) {
            channelPrefix = channelPrefix.append(Component.text(
                    "<" + channel.getName() + "> ", NamedTextColor.GOLD
            ));
        }
        Component channelJoinMessage = channelPrefix.append(Component.text(
                sender.getName() + " left the channel.", NamedTextColor.WHITE
        ));
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ChatChannel channel : channels) {
                if (channel.isJoined(player.getUniqueId()) && channel.hasUserRights(player)) {
                    player.sendMessage(channelJoinMessage);
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event) {
        ArrayList<ChatChannel> channels = new ArrayList<>();
        for (ChatChannel channel : basicsModule.getChatChannelManager().getChannels()) {
            if (channel.isJoined(event.getPlayer().getUniqueId()) && !channel.hasUserRights(event.getPlayer())) {
                channel.leave(event.getPlayer().getUniqueId()); // might have been removed since joining, so silently leave
            }
            if (channel.isJoined(event.getPlayer().getUniqueId()) && channel.hasUserRights(event.getPlayer())) {
                channels.add(channel);
            }
        }
        Bukkit.getScheduler().runTask(basicsModule.getPlugin(), () -> sendJoinChannelsMessage(event.getPlayer(), channels.toArray(new ChatChannel[0])));
    }

}
