package com.froobworld.nabsuite.modules.basics.channel;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChannelDeletionPolicyEnforcer {
    private static final long TIME_TO_DELETE = TimeUnit.DAYS.toMillis(60); // delete after 60 days of inactivity
    private final BasicsModule basicsModule;

    public ChannelDeletionPolicyEnforcer(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(basicsModule.getPlugin(), this::checkChannels, 1, 20 * 60 * 60); // check every hour
    }

    public void checkChannels() {
        List<ChatChannel> channelsToRemove = new ArrayList<>();
        for (ChatChannel channel : basicsModule.getChatChannelManager().getChannels()) {
            if (channel.getLastMessageTime() <= 0) {
                channel.updateLastMessageTime(); // in case there are no messages yet, initialise the time
            }
            if (System.currentTimeMillis() - channel.getLastMessageTime() > TIME_TO_DELETE) {
                channelsToRemove.add(channel);
            }
        }
        for (ChatChannel channel : channelsToRemove) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (channel.isJoined(player.getUniqueId())) {
                    channel.leave(player.getUniqueId());
                }
            }
            PlayerIdentity playerIdentity = basicsModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(channel.getCreator());
            if (playerIdentity != null) {
                basicsModule.getMailCentre().sendMail(
                        playerIdentity.getUuid(),
                        ConsoleUtils.CONSOLE_UUID,
                        "Your chat channel '" + channel.getName() + "' has been deleted due to inactivity."
                );
            }
            basicsModule.getChatChannelManager().deleteChannel(channel);
        }
    }

}
