package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class NoExplodeFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoExplodeFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canExplode(Location location) {
        for (AreaLike area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_EXPLODE)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!canExplode(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

}
