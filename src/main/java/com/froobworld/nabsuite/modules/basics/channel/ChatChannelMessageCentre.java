package com.froobworld.nabsuite.modules.basics.channel;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.ComponentUtils;
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

public class ChatChannelMessageCentre implements Listener {
    private static final String LAST_CHANNEL_KEY = "last-chat-channel";
    private final BasicsModule basicsModule;

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
        Component messageComponent = Component.text(message);
        if (adminModule != null) {
            messageComponent = adminModule.getProfanityFilter().filter(messageComponent);
        }
        String format = basicsModule.getConfig().chatChannelFormat.get();
        Component component = MiniMessage.miniMessage().deserialize(
                format,
                TagResolver.resolver("display_name", Tag.inserting(sender.displayName())),
                TagResolver.resolver("channel", Tag.inserting(Component.text(channel.getName()))),
                TagResolver.resolver("message", Tag.inserting(ComponentUtils.clickableUrls(messageComponent)))
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (channel.isJoined(player.getUniqueId()) && channel.hasUserRights(player)) {
                if (basicsModule.getPlayerDataManager().getIgnoreManager().isIgnoring(player, sender)) {
                    continue;
                }
                player.sendMessage(component);
            }
        }
        channel.updateLastMessageTime();
        PlayerVars playerVars = basicsModule.getPlugin().getPlayerVarsManager().getVars(sender.getUniqueId());
        playerVars.put(LAST_CHANNEL_KEY, channel.getName());
    }

    public void replyChannel(Player sender, String message) {
        PlayerVars playerVars = basicsModule.getPlugin().getPlayerVarsManager().getVars(sender.getUniqueId());
        String lastChannelName = playerVars.get(LAST_CHANNEL_KEY, String.class);
        ChatChannel lastChannel = lastChannelName == null ? null : basicsModule.getChatChannelManager().getChannel(lastChannelName);
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
            Component nextPrefix = MiniMessage.miniMessage().deserialize(
                    basicsModule.getConfig().chatChannelPrefix.get(),
                    TagResolver.resolver("channel", Tag.inserting(Component.text(channel.getName())))
            );
            channelPrefix = channelPrefix.append(nextPrefix);
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
            Component nextPrefix = MiniMessage.miniMessage().deserialize(
                    basicsModule.getConfig().chatChannelPrefix.get(),
                    TagResolver.resolver("channel", Tag.inserting(Component.text(channel.getName())))
            );
            channelPrefix = channelPrefix.append(nextPrefix);
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
