package com.froobworld.nabsuite.modules.admin.suspicious.monitors;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

public class GriefMonitor implements ActivityMonitor, Listener {
    private static final MaterialSetTag TRACKABLE_MATERIALS = new MaterialSetTag(NamespacedKey.fromString("trackable_materials"))
            .add(Material.CHEST, Material.BARREL)
            .add(MaterialTags.SHULKER_BOXES);
    private static final int SPAWN_DISTANCE_THRESHOLD = 300;
    private static final int DEFICIT_SUSPICION_THRESHOLD = 10;
    private final NamespacedKey pdcKey;
    private final BasicsModule basicsModule;

    public GriefMonitor(AdminModule adminModule) {
        this.basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        pdcKey = new NamespacedKey(adminModule.getPlugin(), "grief-monitor-deficit");
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @Override
    public double getSuspicionLevel(Player player) {
        int deficit = getTrackableBlockDeficit(player);
        return deficit < 0 ? 0 : Math.min(2, 2.0 * (double) deficit / (double) DEFICIT_SUSPICION_THRESHOLD);
    }

    private int getTrackableBlockDeficit(Player player) {
        return player.getPersistentDataContainer().getOrDefault(pdcKey, PersistentDataType.INTEGER, 0);
    }

    private void breakTrackableBlock(Player player) {
        player.getPersistentDataContainer().set(pdcKey, PersistentDataType.INTEGER, getTrackableBlockDeficit(player) + 1);
    }

    private void placeTrackableBlock(Player player) {
        player.getPersistentDataContainer().set(pdcKey, PersistentDataType.INTEGER, getTrackableBlockDeficit(player) - 1);
    }

    private boolean isInSpawn(Location location) {
        Location spawnLocation = basicsModule.getSpawnManager().getSpawnLocation();
        return Math.max(Math.abs(location.getBlockX() - spawnLocation.getBlockX()), Math.abs(location.getBlockZ() - spawnLocation.getBlockZ())) <= SPAWN_DISTANCE_THRESHOLD;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (TRACKABLE_MATERIALS.isTagged(event.getBlock()) && isInSpawn(event.getBlock().getLocation())) {
            placeTrackableBlock(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockBreak(BlockBreakEvent event) {
        if (TRACKABLE_MATERIALS.isTagged(event.getBlock()) && isInSpawn(event.getBlock().getLocation())) {
            breakTrackableBlock(event.getPlayer());
        }
    }

}
