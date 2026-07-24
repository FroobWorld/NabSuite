package com.froobworld.nabsuite.modules.admin.suspicious.monitors;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
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
            .add(MaterialSetTag.SHULKER_BOXES.getValues())
            .add(MaterialSetTag.COPPER_CHESTS.getValues());
    private static final int DEFICIT_SUSPICION_THRESHOLD = 10;
    private final NamespacedKey pdcKey;
    private final ProtectModule protectModule;

    public GriefMonitor(AdminModule adminModule) {
        this.protectModule = adminModule.getPlugin().getModule(ProtectModule.class);
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

    private boolean isInMonitoredArea(Location location) {
        return protectModule.getAreaManager().getTopMostAreasAtLocation(location).stream().anyMatch(a -> a.hasFlag(Flags.MONITOR_GRIEF));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (TRACKABLE_MATERIALS.isTagged(event.getBlock()) && isInMonitoredArea(event.getBlock().getLocation())) {
            placeTrackableBlock(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockBreak(BlockBreakEvent event) {
        if (TRACKABLE_MATERIALS.isTagged(event.getBlock()) && isInMonitoredArea(event.getBlock().getLocation())) {
            breakTrackableBlock(event.getPlayer());
        }
    }

}
