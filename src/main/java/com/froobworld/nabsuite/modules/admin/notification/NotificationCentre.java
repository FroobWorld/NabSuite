package com.froobworld.nabsuite.modules.admin.notification;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class NotificationCentre {
    private final Map<String, String> notificationPermissionMap = new HashMap<>();
    private final Map<String, Map<UUID, Set<UUID>>> notificationIgnoreMap = new HashMap<>();

    public NotificationCentre() {

    }

    public void registerNotificationKey(String notificationKey, String permission) {
        notificationPermissionMap.put(notificationKey, permission);
        notificationIgnoreMap.put(notificationKey, new HashMap<>());
    }

    public void sendNotification(String notificationKey, Component notification) {
        sendNotification(notificationKey, notification, null, null);
    }

    public void sendNotification(String notificationKey, Component notification, UUID source, Predicate<Player> filter) {
        String permission = notificationPermissionMap.get(notificationKey);
        Map<UUID, Set<UUID>> ignoreMap = notificationIgnoreMap.get(notificationKey);
        if (permission == null || ignoreMap == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                if (filter != null && !filter.test(player)) {
                    continue;
                }
                if (source != null && getIgnoredSources(notificationKey, player).contains(source)) {
                    // Skip notifications for ignored sources
                    continue;
                }
                player.sendMessage(notification);
            }
        }
        Bukkit.getConsoleSender().sendMessage(notification);
    }

    public void setIgnoreSource(String notificationKey, Player player, UUID source, boolean value) {
        Map<UUID, Set<UUID>> ignoreMap = notificationIgnoreMap.get(notificationKey);
        if (ignoreMap == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        if (value) {
            ignoreMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(source);
        } else {
            ignoreMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).remove(source);
        }
    }

    public Set<UUID> getIgnoredSources(String notificationKey, Player player) {
        Map<UUID, Set<UUID>> ignoreMap = notificationIgnoreMap.get(notificationKey);
        if (ignoreMap == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        return ignoreMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
    }

}
