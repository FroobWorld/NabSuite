package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import com.froobworld.nabsuite.modules.protect.vehicle.VehicleTracker;
import org.bukkit.Location;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class NoInteractFlagEnforcer implements Listener {
    private final AreaManager areaManager;
    private final VehicleTracker vehicleTracker;

    public NoInteractFlagEnforcer(AreaManager areaManager, VehicleTracker vehicleTracker) {
        this.areaManager = areaManager;
        this.vehicleTracker = vehicleTracker;
    }

    private boolean canInteract(Location location, Player player, boolean informOnFail) {
        for (AreaLike area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_INTERACT) && !area.hasUserRights(player)) {
                if (informOnFail) {
                    areaManager.getAreaNotificationManager().notifyProtected(player);
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!canInteract(event.getClickedBlock().getLocation(), event.getPlayer(), event.getAction() != Action.PHYSICAL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (vehicleTracker.hasAccessed(event.getRightClicked(), event.getPlayer())) {
            return;
        }
        if (!canInteract(event.getRightClicked().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (vehicleTracker.hasAccessed(event.getRightClicked(), event.getPlayer())) {
            return;
        }
        if (!canInteract(event.getRightClicked().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (vehicleTracker.hasAccessed(event.getEntity(), event.getPlayer())) {
            return;
        }
        if (!canInteract(event.getEntity().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (vehicleTracker.hasAccessed(event.getEntity(), event.getPlayer())) {
            return;
        }
        if (!canInteract(event.getEntity().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (!canInteract(event.getEntity().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCombustEntity(EntityCombustByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getCombuster());
        if (causer == null) {
            return;
        }
        if (!canInteract(event.getEntity().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamager());
        if (causer == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (event.getEntity() instanceof Monster && event.getEntity().customName() == null && causer.equals(((Monster) event.getEntity()).getTarget())) {
            return; // Allow the killing of un-named monsters that are attacking the player
        }
        if (vehicleTracker.hasAccessed(event.getEntity(), causer)) {
            return;
        }
        if (!canInteract(event.getEntity().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getAttacker());
        if (causer == null) {
            return;
        }
        if (vehicleTracker.hasAccessed(event.getVehicle(), causer)) {
            return;
        }
        if (!canInteract(event.getVehicle().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getAttacker());
        if (causer == null) {
            return;
        }
        if (vehicleTracker.hasAccessed(event.getVehicle(), causer)) {
            return;
        }
        if (!canInteract(event.getVehicle().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getEntered());
        if (causer == null) {
            return;
        }
        if (vehicleTracker.hasAccessed(event.getVehicle(), causer)) {
            return;
        }
        if (!canInteract(event.getVehicle().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onArmourStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!canInteract(event.getRightClicked().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        if (!canInteract(event.getBlock().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }


}
