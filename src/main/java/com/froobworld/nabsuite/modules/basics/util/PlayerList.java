package com.froobworld.nabsuite.modules.basics.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class PlayerList {

    private PlayerList() {}

    public static List<Component> getDisplayNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::displayName)
                .collect(Collectors.toList());
    }

    public static Component getPlayerList(Component separator) {
        return Component.join(JoinConfiguration.separator(separator), getDisplayNames());
    }

    public static Component getPlayerList() {
        return getPlayerList(Component.text(", "));
    }

}
