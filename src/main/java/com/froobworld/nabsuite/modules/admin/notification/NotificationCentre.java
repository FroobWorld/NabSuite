package com.froobworld.nabsuite.modules.admin.notification;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NotificationCentre {
    private final Map<String, String> notificationPermissionMap = new HashMap<>();

    public NotificationCentre() {

    }

    public void registerNotificationKey(String notificationKey, String permission) {
        notificationPermissionMap.put(notificationKey, permission);
    }

    public void sendNotification(String notificationKey, Component notification) {
        String permission = notificationPermissionMap.get(notificationKey);
        if (permission == null) {
            throw new IllegalArgumentException("Unknown notification key '" + notificationKey + "'");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(notification);
            }
        }
        Bukkit.getConsoleSender().sendMessage(notification);
    }

}
