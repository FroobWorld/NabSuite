package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KeepInventoryFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public KeepInventoryFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean shouldKeepInventory(Location location) {
        for (Area area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.KEEP_INVENTORY)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (shouldKeepInventory(event.getEntity().getLocation())) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
        }
    }

}
