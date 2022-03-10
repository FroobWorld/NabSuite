package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class NoMobDamageFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoMobDamageFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canMobDamage(Location targetLocation, Location mobLocation) {
        for (AreaLike area : Sets.union(areaManager.getTopMostAreasAtLocation(targetLocation), areaManager.getTopMostAreasAtLocation(mobLocation))) {
            if (area.hasFlag(Flags.NO_MOB_DAMAGE)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (PlayerCauser.getPlayerCauser(event.getDamager()) != null) {
            return;
        }
        if (!canMobDamage(event.getEntity().getLocation(), event.getDamager().getLocation())) {
            event.setCancelled(true);
        }
    }

}
