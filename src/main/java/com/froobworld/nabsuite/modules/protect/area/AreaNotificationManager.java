package com.froobworld.nabsuite.modules.protect.area;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class AreaNotificationManager {
    private static final Component AREA_PROTECTED_MESSAGE = Component.text("This area is protected.").color(NamedTextColor.RED);
    private static final long NOTIFICATION_RATE_LIMIT = TimeUnit.MILLISECONDS.toMillis(500);
    private final Map<Player, Long> lastNotifyMap = new WeakHashMap<>();

    public void notifyProtected(Player player) {
        notifyProtected(player, AREA_PROTECTED_MESSAGE);
    }

    public void notifyProtected(Player player, Component message) {
        if (System.currentTimeMillis() - lastNotifyMap.getOrDefault(player, -1L) > NOTIFICATION_RATE_LIMIT) {
            player.sendMessage(message);
            lastNotifyMap.put(player, System.currentTimeMillis());
        }
    }

}
