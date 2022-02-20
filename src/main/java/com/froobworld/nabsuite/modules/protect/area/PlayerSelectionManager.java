package com.froobworld.nabsuite.modules.protect.area;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSelectionManager {
    private final Map<UUID, Location> corner1Map = new HashMap<>();
    private final Map<UUID, Location> corner2Map = new HashMap<>();

    public void setCorner1(Player player, Location corner1) {
        corner1Map.put(player.getUniqueId(), corner1);
    }

    public void setCorner2(Player player, Location corner2) {
        corner2Map.put(player.getUniqueId(), corner2);
    }

    public Location getCorner1(Player player) {
        return corner1Map.get(player.getUniqueId());
    }

    public Location getCorner2(Player player) {
        return corner2Map.get(player.getUniqueId());
    }

}
