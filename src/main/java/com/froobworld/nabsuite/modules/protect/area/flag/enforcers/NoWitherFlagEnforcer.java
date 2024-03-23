package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import org.bukkit.Location;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class NoWitherFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoWitherFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canWither(Location location) {
        for (AreaLike area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_WITHER)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            if (!canWither(event.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof Wither wither) {
            if (!canWither(wither.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof Wither) {
            if (!canWither(event.getEntity().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

}
