package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class NoBuildFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoBuildFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canBuild(Location location, Player player, boolean informOnFail) {
        for (AreaLike area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_BUILD) && !area.hasUserRights(player)) {
                if (informOnFail) {
                    areaManager.getAreaNotificationManager().notifyProtected(player);
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!canBuild(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getRemover());
        if (causer == null) {
            return;
        }
        if (!canBuild(event.getEntity().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getAttacker());
        if (causer == null) {
            return;
        }
        if (!canBuild(event.getVehicle().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onHitArmourStand(EntityDamageByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamager());
        if (causer == null) {
            return;
        }
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        if (!canBuild(event.getEntity().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }


}
