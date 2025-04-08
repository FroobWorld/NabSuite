package com.froobworld.nabsuite.modules.basics.nametag;

import com.froobworld.nabsuite.modules.basics.config.BasicsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class NameTagFeature implements Comparable<NameTagFeature> {
    public interface Handler {
        boolean isNameTagFeatureActive(Player player);
    }

    private final String name;
    private final Component prefix;
    private final Component suffix;
    private final NamedTextColor color;
    private final int priority;

    private final Handler handler;

    public NameTagFeature(String name, Handler handler, BasicsConfig.NameTagSettings config) {
        this.name = name;
        this.handler = handler;

        priority = config.priority.get();
        prefix = MiniMessage.miniMessage().deserialize(config.prefix.get());
        suffix = MiniMessage.miniMessage().deserialize(config.suffix.get());
        color = NamedTextColor.NAMES.value(config.color.get().toLowerCase());
    }

    public String getName() {
        return name;
    }

    public void setupTeam(Team team) {
        team.prefix(team.prefix().append(prefix));
        team.suffix(team.suffix().append(suffix));
        if (color != null) {
            team.color(color);
        }
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission("nabsuite.nametag."+getName());
    }

    public boolean testPlayer(Player player) {
        return handler.isNameTagFeatureActive(player);
    }

    @Override
    public int compareTo(@NotNull NameTagFeature other) {
        if (getPriority() == other.getPriority()) {
            return Comparator.comparing(NameTagFeature::getName).compare(this, other);
        }
        return getPriority() - other.getPriority();
    }

}
