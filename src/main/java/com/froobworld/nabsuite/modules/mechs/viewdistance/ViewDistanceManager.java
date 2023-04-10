package com.froobworld.nabsuite.modules.mechs.viewdistance;

import com.destroystokyo.paper.ClientOption;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewDistanceManager {
    private final NamespacedKey pdcKey;
    private final Map<UUID, Boolean> viewDistanceCappedCache = new HashMap<>();
    private final MechsModule mechsModule;
    private final ViewDistanceEnforcer viewDistanceEnforcer;

    public ViewDistanceManager(MechsModule mechsModule) {
        this.pdcKey = new NamespacedKey(mechsModule.getPlugin(), "view-distance-uncapped");
        this.mechsModule = mechsModule;
        this.viewDistanceEnforcer = new ViewDistanceEnforcer(mechsModule, this);
        Bukkit.getPluginManager().registerEvents(viewDistanceEnforcer, mechsModule.getPlugin());
    }

    public boolean isViewDistanceCapped(Player player) {
        return viewDistanceCappedCache.computeIfAbsent(player.getUniqueId(), k -> !player.getPersistentDataContainer().has(pdcKey));
    }

    public void setViewDistanceCapped(Player player, boolean capped) {
        if (!capped) {
            player.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            player.getPersistentDataContainer().remove(pdcKey);
        }
        viewDistanceCappedCache.put(player.getUniqueId(), capped);
        recalcPlayerViewDistance(player, player.getClientOption(ClientOption.VIEW_DISTANCE));
    }

    public int getMaxViewDistance(Player player) {
        if (isViewDistanceCapped(player)) {
            return mechsModule.getConfig().viewDistance.cappedViewDistance.get();
        } else {
            return mechsModule.getConfig().viewDistance.uncappedViewDistance.get();
        }
    }

    void recalcPlayerViewDistance(Player player, int clientViewDistance) {
        int clampedViewDistance = Math.max(Bukkit.getSimulationDistance(), Math.min(32, Math.min(clientViewDistance, getMaxViewDistance(player))));
        player.setViewDistance(clampedViewDistance);
        mechsModule.getPlugin().getLogger().info("Set view distance of " + player.getName() + " to " + clampedViewDistance + " (client view distance is " + clientViewDistance + ").");
    }

}
