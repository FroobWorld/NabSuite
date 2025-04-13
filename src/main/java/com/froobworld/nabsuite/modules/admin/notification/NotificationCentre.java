package com.froobworld.nabsuite.modules.admin.notification;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class NotificationCentre {
    private final Map<String, String> notificationPermissionMap = new HashMap<>();
    private final Map<String, WeakHashMap<Player, Set<UUID>>> notificationIgnoreMap = new HashMap<>();

    public NotificationCentre() {

    }

    public void registerNotificationKey(String notificationKey, String permission) {
        notificationPermissionMap.put(notificationKey, permission);
        notificationIgnoreMap.put(notificationKey, new WeakHashMap<>());
    }

    public void sendNotification(String notificationKey, Component notification) {
        sendNotification(notificationKey, notification, null);
    }

    public void sendNotification(String notificationKey, Component notification, UUID source) {
        String permission = notificationPermissionMap.get(notificationKey);
        WeakHashMap<Player, Set<UUID>> ignoreMap = notificationIgnoreMap.get(notificationKey);
        if (permission == null || ignoreMap == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                if ("ore-alert".equals(notificationKey) && player.getUniqueId().equals(source)) {
                    // Don't send ore alerts to the player themselves, it would reveal how many ores they're expected to find
                    continue;
                }
                if (source != null && ignoreMap.containsKey(player) && ignoreMap.get(player).contains(source)) {
                    // Skip notifications for ignored sources
                    continue;
                }
                player.sendMessage(notification);
            }
        }
        Bukkit.getConsoleSender().sendMessage(notification);
    }

    public void setIgnoreSource(String notificationKey, Player player, UUID source, boolean value) {
        WeakHashMap<Player, Set<UUID>> ignoreMap = notificationIgnoreMap.get(notificationKey);
        if (ignoreMap == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        if (value) {
            ignoreMap.computeIfAbsent(player, k -> new HashSet<>()).add(source);
        } else {
            ignoreMap.computeIfAbsent(player, k -> new HashSet<>()).remove(source);
        }
    }

    public Set<UUID> getIgnoredSources(String notificationKey, Player player) {
        WeakHashMap<Player, Set<UUID>> ignoreMap = notificationIgnoreMap.get(notificationKey);
        if (ignoreMap == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        return ignoreMap.computeIfAbsent(player, k -> new HashSet<>());
    }

}
