package com.froobworld.nabsuite.modules.basics.util;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
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
        return getPlayerList(Component.text(", ", NamedTextColor.WHITE));
    }

    public static List<Component> getDisplayNamesDecorated(BasicsModule basicsModule) {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> {
                    boolean afk = basicsModule.getAfkManager().isAfk(player);
                    Component base = afk ? Component.text("(AFK)", NamedTextColor.GRAY) : Component.empty();
                    return base.append(player.displayName());
                })
                .collect(Collectors.toList());
    }

    public static Component getPlayerListDecorated(Component separator, BasicsModule basicsModule) {
        return Component.join(JoinConfiguration.separator(separator), getDisplayNamesDecorated(basicsModule));
    }

    public static Component getPlayerListDecorated(BasicsModule basicsModule) {
        return getPlayerListDecorated(Component.text(", ", NamedTextColor.WHITE), basicsModule);
    }

}
