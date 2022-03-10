package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class NoMobGriefFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoMobGriefFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canMobGrief(Location location) {
        for (AreaLike area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_MOB_GRIEF)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (PlayerCauser.getPlayerCauser(event.getEntity()) != null) {
            return;
        }
        if (!canMobGrief(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

}
