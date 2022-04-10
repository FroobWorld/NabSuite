package com.froobworld.nabsuite.modules.protect.horse;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class HorseNotificationManager {
    private static final Component HORSE_PROTECTED_MESSAGE = Component.text("This mount is protected.").color(NamedTextColor.RED);
    private static final long NOTIFICATION_RATE_LIMIT = TimeUnit.MILLISECONDS.toMillis(500);
    private final Map<Player, Long> lastNotifyMap = new WeakHashMap<>();

    public void notifyProtected(Player player) {
        if (System.currentTimeMillis() - lastNotifyMap.getOrDefault(player, -1L) > NOTIFICATION_RATE_LIMIT) {
            player.sendMessage(HORSE_PROTECTED_MESSAGE);
            lastNotifyMap.put(player, System.currentTimeMillis());
        }
    }

}
