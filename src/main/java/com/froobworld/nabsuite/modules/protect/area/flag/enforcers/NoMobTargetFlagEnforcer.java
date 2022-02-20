package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class NoMobTargetFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoMobTargetFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canMobTarget(Location targetLocation, Location mobLocation) {
        for (Area area : Sets.union(areaManager.getTopMostAreasAtLocation(targetLocation), areaManager.getTopMostAreasAtLocation(mobLocation))) {
            if (area.hasFlag(Flags.NO_MOB_TARGET)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() == null) {
            return;
        }
        if (!canMobTarget(event.getTarget().getLocation(), event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }

}
