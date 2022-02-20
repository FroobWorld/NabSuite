package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class NoMobSpawnFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoMobSpawnFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canMobSpawn(Location location) {
        for (Area area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_MOB_SPAWN)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCreateSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) {
            return;
        }
        if (!canMobSpawn(event.getLocation())) {
            event.setCancelled(true);
        }
    }

}
